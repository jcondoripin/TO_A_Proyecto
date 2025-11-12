// package com.battlesimulator.usecases;

// import com.battlesimulator.domain.Army;
// import com.battlesimulator.domain.IWarrior;

// public class Battle {
//   private final Army army1;
//   private final Army army2;
//   private final StringBuilder log = new StringBuilder();

//   public Battle(Army army1, Army army2) {
//     this.army1 = army1;
//     this.army2 = army2;
//   }

//   public Army simulate() {
//     log.append("Batalla entre Ejército 1 (").append(army1.getWarriors().size()).append(" guerreros) y Ejército 2 (").append(army2.getWarriors().size()).append(" guerreros)\n");

//     while (!army1.isDefeated() && !army2.isDefeated()) {
//       // Turno army1
//       for (IWarrior w : army1.getWarriors()) {
//         if (!army2.isDefeated()) {
//           IWarrior target = w.chooseTarget(army2);
//           w.attack(target);
//           log.append(w.getName()).append(" ataca a ").append(target.getName()).append(" causando daño. Salud restante: ").append(target.getHealth()).append("\n");
//         }
//       }
//       // Turno army2
//       for (IWarrior w : army2.getWarriors()) {
//         if (!army1.isDefeated()) {
//           IWarrior target = w.chooseTarget(army1);
//           w.attack(target);
//           log.append(w.getName()).append(" ataca a ").append(target.getName()).append(" causando daño. Salud restante: ").append(target.getHealth()).append("\n");
//         }
//       }
//     }

//     Army winner = army1.isDefeated() ? army2 : army1;
//     log.append("Ganador de la batalla: Ejército ").append(winner == army1 ? "1" : "2").append("\n");
//     return winner;
//   }

//   public String getLog() {
//     return log.toString();
//   }
// }