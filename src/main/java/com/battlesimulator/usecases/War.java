// package com.battlesimulator.usecases;

// import com.battlesimulator.domain.Army;
// import com.battlesimulator.domain.Clan;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;
// import java.util.Random;
// import java.util.stream.Collectors;

// public class War {
//   private final Clan clan1;
//   private final Clan clan2;
//   private final StringBuilder warLog = new StringBuilder();

//   public War(Clan clan1, Clan clan2) {
//     this.clan1 = clan1;
//     this.clan2 = clan2;
//   }

//   public Clan simulate() {
//     warLog.append("Guerra entre ").append(clan1.getName()).append(" y ").append(clan2.getName()).append("\n");

//     while (!clan1.isDefeated() && !clan2.isDefeated()) {
//       Army a1 = selectArmy(clan1);
//       Army a2 = selectArmy(clan2);

//       if (a1 == null || a2 == null)
//         break;

//       a1.setHasFought(true);
//       a2.setHasFought(true);

//       Battle battle = new Battle(a1, a2);
//       battle.simulate();
//       warLog.append(battle.getLog());
//     }

//     Clan winner = clan1.isDefeated() ? clan2 : clan1;
//     warLog.append("Ganador de la guerra: ").append(winner.getName()).append("\n");
//     System.out.println(warLog.toString());
//     return winner;
//   }

//   private Army selectArmy(Clan clan) {
//     List<Army> available = clan.getActiveArmies();
//     List<Army> fresh = available.stream().filter(a -> !a.hasFought()).collect(Collectors.toCollection(ArrayList::new));
//     if (!fresh.isEmpty()) {
//       available = fresh;
//     }
//     Collections.sort(available, (a, b) -> Integer.compare(b.getLevel(), a.getLevel()));
//     if (available.isEmpty())
//       return null;
//     Random rand = new Random();
//     return available.get(rand.nextInt(Math.min(3, available.size())));
//   }
// }