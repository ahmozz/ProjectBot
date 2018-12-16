package com.mycompany.projectbot.decision;

import com.mycompany.projectbot.HunterBot;
import org.jpl7.JRef;
import org.jpl7.Query;
import org.jpl7.Term;

import java.util.Map;

/**
 * @author Ahmed El Mokhtar
 * @author Daniel Amaral
 */

public class ExpertSystem {

    private static final String filePath = "src/ES.pl";

    public static Boolean connect() {
        System.out.print("Connect to ES ...");
        String connection = new StringBuffer("consult('").append(filePath).append("')").toString();
        return Query.hasSolution(connection);
    }

    public static void runES(HunterBot bot) {
        System.out.print("Run ES ...");
        JRef jref = new JRef(bot);
        Query q3 = new Query("run", new Term[]{jref});
        Map<String, Term>[] solutions = q3.allSolutions();

        if (!q3.hasSolution()) {
            System.out.println("runES call failed");
            return;
        }
        System.out.println("passed");
    }

}
