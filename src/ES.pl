:- use_module(library(jpl)).
:- use_module(library(when)).

init(I) :-
  jpl_call('com.mycompany.labopart4.HunterBot', 'getBotInstance', [], I).


run(X) :-
(   info_lostEnemy(X), not(info_beingShot(X))
->  runAroundForItems(X)
;   true
),

(   info_lostEnemy(X)
->  findEnemy(X)
;   true
),

(   not(info_lostEnemy(X)), info_seesEnemy(X), info_weaponReady(X)
->  shootAtEnemy(X)
;   true
),

(   info_isShooting(X), not(info_seesEnemy(X))
->  stop_shooting(X)
;   true
),

(   info_beingShot(X), not(info_seesEnemy(X))
->  turnAroundToFindEnemy(X)
;   true
),

(   enemyToPursue(X)
->  pursueEnemy(X)
;   true
),

(   info_lostEnemy(X), not(info_beingShot(X)), info_beingSrlyInjured(X)
->  findMedKit(X)
;   true
).

info_weaponReady(X) :-
  jpl_call(X, 'weaponReady', [], R),
  jpl_is_true(R).

info_lostEnemy(X) :-
  jpl_call(X, 'lostEnemy', [], R),
  jpl_is_true(R).

findEnemy(X) :-
  jpl_call(X, 'findEnemy', [], R).

info_seesEnemy(X) :-
  jpl_call(X, 'seesEnemy', [], A),
  jpl_is_true(A).

info_isShooting(X) :-
  jpl_call(X, 'isShooting', [], A),
  jpl_is_true(A).

stop_shooting(X) :-
  jpl_call(X, 'stopShooting', [], A).

runAroundForItems(X) :-
  jpl_call(X, 'stateRunAroundItems', [], R).

shootAtEnemy(X) :-
  jpl_call(X, 'shootAtEnemy', [], R).

turnAroundToFindEnemy(X) :-
  jpl_call(X, 'stateHit', [], R).

pursueEnemy(X) :-
  jpl_call(X, 'statePursue', [], R).

findMedKit(X) :-
  jpl_call(X, 'stateMedKit', [], R).

info_beingSrlyInjured(X) :-
  jpl_call(X, 'getInfo', [], I),
  jpl_call(I, 'getHealth', [], H),
  jpl_call(H, 'intValue', [], V),
  V<75.

enemyToPursue(X) :-
  not(info_lostEnemy(X)), not(info_seesEnemy(X)), info_weaponReady(X).

info_beingShot(X) :-
  jpl_call(X, 'getSenses', [], S),
  jpl_call(S, 'isBeingDamaged', [], P),
  jpl_is_true(P).


close :-
    halt.

t :-
    throw( 'this is an error message').

display(X) :-
    write( X).
