package com.mycompany.projectbot;

import com.mycompany.projectbot.decision.ExpertSystem;
import com.mycompany.projectbot.decision.WeaponChoice;
import com.mycompany.projectbot.enumeration.DistanceRange;
import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPref;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Rotate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.*;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.collections.MyCollections;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Ahmed El Mokhtar
 * @author Daniel Amaral
 */

@AgentScoped
public class HunterBot extends UT2004BotModuleController<UT2004Bot> {

    public static long lStartTime;
    public static long lEndTime;
    public static double timeElapsed;
    public static int opponentDamage = 0;
    public static double distance;
    public static Integer weaponQMatrixIndex = null;
    public static Integer lastOpponentHealth = 100;

    public HunterBot() {
        ExpertSystem.connect();
    }


    public void shootAtEnemy() {
        if (enemy.isVisible()) {
            distance = info.getLocation().getDistance(enemy.getLocation());
            weaponQMatrixIndex = WeaponChoice.getBestChoiceIndex(distance);
            WeaponPref weaponPref = WeaponChoice.getBestChoice(distance);
            if (shoot.shoot(weaponPref, enemy)) {

                log.info("Shooting at enemy!!!");

                //start
                lStartTime = System.currentTimeMillis();
            }
        }
    }

    public void stopShooting() {
        System.out.println("**************************************************************************************************   YESSSSS");
        getAct().act(new StopShooting());

        //end
        lEndTime = System.currentTimeMillis();

        //time elapsed
        timeElapsed = (double) (lEndTime - lStartTime) / 1000;

        DistanceRange distanceRange = DistanceRange.getDistanceRange(distance);

        double value = opponentDamage / timeElapsed;
        opponentDamage = 0;

        WeaponChoice.updateQMatrix(distanceRange.getQMatrixindex(), weaponQMatrixIndex, value);
    }

    public boolean enemyIsFar() {
        if (null == enemy || !enemy.isVisible()) {
            return true;
        }
        double distance = info.getLocation().getDistance(enemy.getLocation());
        int decentDistance = Math.round(random.nextFloat() * 800) + 200;
        return decentDistance < distance;
    }

    public boolean isShooting() {
        return (info.isShooting() || info.isSecondaryShooting());
    }

    public void findEnemy() {
        if (null == enemy) {
            // pick new enemy
            enemy = players.getNearestVisiblePlayer(players.getVisibleEnemies().values());
        }
    }

    public boolean lostEnemy() {
        return null == enemy;
    }

    public boolean seesEnemy() {
        if (null != enemy) {
            return enemy.isVisible();
        }
        return false;
    }

    public boolean weaponReady() {
        return weaponry.hasLoadedWeapon();
    }

    public boolean enemyToPersue() {
        return (enemy != null && !enemy.isVisible() && weaponry.hasLoadedWeapon());
    }

    public boolean shooting = false;


    /**
     * how many bot the hunter killed other bots (i.e., bot has fragged them /
     * got point for killing somebody)
     */
    @JProp
    public int frags = 0;

    /**
     * {@link PlayerKilled} listener that provides "frag" counting + is switches
     * the state of the hunter.
     *
     * @param event
     */
    @EventListener(eventClass = PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {
        if (event.getKiller().equals(info.getId())) {
            ++frags;
            //end
            /*
            lEndTime = System.currentTimeMillis();
            //time elapsed
            timeElapsed = (double)(lEndTime - lStartTime)/1000;
            double value = opponentDamage/timeElapsed;
            DistanceRange distanceRange = DistanceRange.getDistanceRange(distance);
            opponentDamage = 0;
            WeaponChoice.updateQMatrix(distanceRange.getQMatrixindex(), weaponQMatrixIndex, value);
            */
            opponentDamage = lastOpponentHealth;
            lastOpponentHealth = 100;
            stopShooting();
        }
        if (enemy == null) {
            return;
        }
        if (enemy.getId().equals(event.getId())) {
            enemy = null;
        }
    }

    public Player enemy = null;

    public Item item = null;

    public TabooSet<Item> tabooItems = null;

    private UT2004PathAutoFixer autoFixer;

    private static int instanceCount = 0;

    @Override
    public void prepareBot(UT2004Bot bot) {
        tabooItems = new TabooSet<Item>(bot);

        autoFixer = new UT2004PathAutoFixer(bot, navigation.getPathExecutor(), fwMap, aStar, navBuilder); // auto-removes wrong navigation links between navpoints

        // listeners        
        navigation.getState().addListener(new FlagListener<NavigationState>() {

            @Override
            public void flagChanged(NavigationState changedValue) {
                switch (changedValue) {
                    case PATH_COMPUTATION_FAILED:
                    case STUCK:
                        if (item != null) {
                            tabooItems.add(item, 10);
                        }
                        reset();
                        break;

                    case TARGET_REACHED:
                        reset();
                        break;
                }
            }
        });

        // DEFINE WEAPON PREFERENCES
        weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.SHOCK_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, false);
        weaponPrefs.addGeneralPref(UT2004ItemType.FLAK_CANNON, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.FLAK_CANNON, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.ROCKET_LAUNCHER, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.BIO_RIFLE, true);

        WeaponChoice.setPossibleWeaponPrefs(weaponPrefs.getPreferredWeapons().toArray(new WeaponPref[weaponPrefs.getPreferredWeapons().size()]));
    }

    /**
     * Here we can modify initializing command for our bot.
     *
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        // just set the name of the bot and his skill level, 1 is the lowest, 7 is the highest
        // skill level affects how well will the bot aim
        return new Initialize().setName("Hunter-" + (++instanceCount)).setDesiredSkill(5);
    }

    /**
     * Resets the state of the Hunter.
     */

    public void looseIntereset() {
        enemy = null;
        bot.getBotName().setInfo("LOOSE INTEREST");
        pursueCount = 0;
    }

    public void reset() {
        bot.getBotName().setInfo("RESET");
        item = null;
        navigation.stopNavigation();
        itemsToRunAround = null;
    }

    @EventListener(eventClass = PlayerDamaged.class)
    public void playerDamaged(PlayerDamaged event) {
        log.info("I have just hurt other bot for: " + event.getDamageType() + "[" + event.getDamage() + "]");
        opponentDamage += event.getDamage();
        lastOpponentHealth -= event.getDamage();
    }

    @EventListener(eventClass = BotDamaged.class)
    public void botDamaged(BotDamaged event) {
        log.info("I have just been hurt by other bot for: " + event.getDamageType() + "[" + event.getDamage() + "]");
    }

    /**
     * Main method that controls the bot - makes decisions what to do next. It
     * is called iteratively by Pogamut engine every time a synchronous batch
     * from the environment is received. This is usually 4 times per second - it
     * is affected by visionTime variable, that can be adjusted in GameBots ini
     * file in UT2004/System folder.
     *
     * @throws cz.cuni.amis.pogamut.base.exceptions.PogamutException
     */
    @Override
    public void logic() {
        ExpertSystem.runES(this);
    }

    ///////////////
    // STATE HIT //
    ///////////////
    public void stateHit() {
        //log.info("Decision is: HIT");
        bot.getBotName().setInfo("TURNING AROUND");
        System.out.println("/////////////////////////////////////////////////////////////////////////////////////////");
        System.out.println(getInfo().getHealth().intValue());
        System.out.println("/////////////////////////////////////////////////////////////////////////////////////////");
        bot.getBotName().setInfo("HIT");
        if (navigation.isNavigating()) {
            navigation.stopNavigation();
            item = null;
        }
        getAct().act(new Rotate().setAmount(32000));
    }

    //////////////////
    // STATE PURSUE //
    //////////////////

    /**
     * State pursue is for pursuing enemy who was for example lost behind a
     * corner. How it works?: <ol> <li> initialize properties <li> obtain path
     * to the enemy <li> follow the path - if it reaches the end - set lastEnemy
     * to null - bot would have seen him before or lost him once for all </ol>
     */
    public void statePursue() {
        //log.info("Decision is: PURSUE");
        bot.getBotName().setInfo("PURSUE");
        ++pursueCount;
        System.out.println("_______________________________________________________________________________________" + pursueCount);
        if (pursueCount > 30) {
            looseIntereset();
            reset();
        }
        if (enemy != null) {
            bot.getBotName().setInfo("NAVIGATE TO ENEMY");
            navigation.navigate(enemy);
            if (!navigation.isNavigating()) {
                stateRunAroundItems();
            }
            item = null;
        }
    }

    public int pursueCount = 0;

    //////////////////
    // STATE MEDKIT //
    //////////////////
    public void stateMedKit() {
        bot.getBotName().setInfo("MEDKIT");
        log.info("Decision is: MEDKIT");
        Item item = items.getPathNearestSpawnedItem(ItemType.Category.HEALTH);
        if (item == null) {
            log.warning("NO HEALTH ITEM TO RUN TO => ITEMS");
            stateRunAroundItems();
        } else {
            //bot.getBotName().setInfo("MEDKIT");
            navigation.navigate(item);
            this.item = item;
        }
    }

    ////////////////////////////
    // STATE RUN AROUND ITEMS //
    ////////////////////////////
    public List<Item> itemsToRunAround = null;

    public void stateRunAroundItems() {
        //log.info("Decision is: ITEMS");
        //config.setName("Hunter [ITEMS]");
        bot.getBotName().setInfo("RUN AROUND");
        if (navigation.isNavigatingToItem()) return;

        List<Item> interesting = new ArrayList<Item>();

        // ADD WEAPONS
        for (ItemType itemType : ItemType.Category.WEAPON.getTypes()) {
            if (!weaponry.hasLoadedWeapon(itemType)) interesting.addAll(items.getSpawnedItems(itemType).values());
        }
        // ADD ARMORS
        for (ItemType itemType : ItemType.Category.ARMOR.getTypes()) {
            interesting.addAll(items.getSpawnedItems(itemType).values());
        }
        // ADD QUADS
        interesting.addAll(items.getSpawnedItems(UT2004ItemType.U_DAMAGE_PACK).values());
        // ADD HEALTHS
        if (info.getHealth() < 100) {
            interesting.addAll(items.getSpawnedItems(UT2004ItemType.HEALTH_PACK).values());
        }

        Item item = MyCollections.getRandom(tabooItems.filter(interesting));
        if (item == null) {
            log.warning("NO ITEM TO RUN FOR!");
            if (navigation.isNavigating()) return;
            bot.getBotName().setInfo("RANDOM NAV");
            navigation.navigate(navPoints.getRandomNavPoint());
        } else {
//            Integer i = new Integer(1);
            //          i.intValue()
            this.item = item;
            log.info("RUNNING FOR: " + item.getType().getName());
            bot.getBotName().setInfo("ITEM: " + item.getType().getName() + "");
            navigation.navigate(item);
        }
    }

    ////////////////
    // BOT KILLED //
    ////////////////
    @Override
    public void botKilled(BotKilled event) {
        //end
        /*
        lEndTime = System.currentTimeMillis();
        //time elapsed
        timeElapsed = (double)(lEndTime - lStartTime)/1000;
        double value = opponentDamage/timeElapsed;
        opponentDamage = 0;
        DistanceRange distanceRange = DistanceRange.getDistanceRange(distance);
        WeaponChoice.updateQMatrix(distanceRange.getQMatrixindex(), weaponQMatrixIndex, value);
        reset();
        */
        stopShooting();
    }

    ///////////////////////////////////
    public static void main(String args[]) throws PogamutException {
        // starts 3 Hunters at once
        // note that this is the most easy way to get a bunch of (the same) bots running at the same time        
        new UT2004BotRunner(HunterBot.class, "Hunter").setMain(true).setLogLevel(Level.INFO).startAgents(1);
    }
}
