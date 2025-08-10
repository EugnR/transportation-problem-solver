package org.example;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;



public class TransportProblemSolver {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Выберите метод построения опорного плана:");
        System.out.println("1 - Северо-западный угол");
        System.out.println("2 - Метод минимальных значений");
        System.out.print("Ваш выбор: ");
        int choice = scanner.nextInt();

//        int[] supply = {200, 300, 250};
//        int[] demand = {210, 150, 120, 135, 135};
//
//        int[][] cost = {
//                {20, 10, 13, 13, 18},
//                {27, 19, 20, 16, 22},
//                {36, 17, 19, 21, 23}
//        };

        //Мой вариант контрольной
//        int[] supply = {70, 50, 150, 330};
//        int[] demand = {20, 40, 75, 25, 200, 140, 100};
//
//        int[][] cost = {
//                {8, 1, 9,3,6,7,2},
//                {1,2,2,4,1,8,3},
//                {5,3,1,3,2,5,8},
//                {2,5,7,1,6,3,4}
//        };

        //Мой вариант контрольной
        int[] supply = {30,190,250};
        int[] demand = {70,120,150,130};

        int[][] cost = {
                {4,7,2,3},
                {3,1,0,4},
                {5,6,3,7}
        };

        int[][] allocation;

        if (choice == 1) {
            allocation = northwestCorner(supply.clone(), demand.clone());
            System.out.println("Начальный опорный план северо-западного метода:");
        } else if (choice == 2) {
            allocation = minimumCostMethod(supply.clone(), demand.clone(), cost);
            System.out.println("Начальный опорный план метода минимальных значений:");
        } else {
            System.out.println("Неверный выбор.");
            return;
        }

        printStartingPlan(allocation);
        optimizeByPotentials(allocation, cost);
    }

    public static int[][] northwestCorner(int[] supply, int[] demand) {
        int rows = supply.length;
        int cols = demand.length;
        int[][] result = new int[rows][cols];

        // Заполняем таблицу -1 (чтобы отличать от нуля как "нет поставки")
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = -1;
            }
        }

        int i = 0, j = 0;

        while (i < rows && j < cols) {
            int allocated = Math.min(supply[i], demand[j]);
            result[i][j] = allocated;
            supply[i] -= allocated;
            demand[j] -= allocated;

            if (supply[i] == 0) {
                i++;
            } else {
                j++;
            }
        }

        return result;
    }

    public static int[][] minimumCostMethod(int[] supply, int[] demand, int[][] cost) {
        int rows = supply.length;
        int cols = demand.length;
        int[][] allocation = new int[rows][cols];

        // Заполняем таблицу -1 (чтобы отличать от 0 как "нет поставки")
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                allocation[i][j] = -1;
            }
        }

        boolean[][] used = new boolean[rows][cols]; // уже использованные ячейки

        while (true) {
            // Найдём минимальную стоимость среди ещё неиспользованных ячеек
            int minCost = Integer.MAX_VALUE;
            int minI = -1, minJ = -1;
            for (int i = 0; i < rows; i++) {
                if (supply[i] == 0) continue;
                for (int j = 0; j < cols; j++) {
                    if (demand[j] == 0 || used[i][j]) continue;
                    if (cost[i][j] < minCost) {
                        minCost = cost[i][j];
                        minI = i;
                        minJ = j;
                    }
                }
            }

            if (minI == -1 || minJ == -1) break; // Все ячейки распределены

            int allocated = Math.min(supply[minI], demand[minJ]);
            allocation[minI][minJ] = allocated;
            supply[minI] -= allocated;
            demand[minJ] -= allocated;

            // Помечаем ячейку как использованную
            if (supply[minI] == 0) {
                for (int j = 0; j < cols; j++) {
                    used[minI][j] = true;
                }
            }
            if (demand[minJ] == 0) {
                for (int i = 0; i < rows; i++) {
                    used[i][minJ] = true;
                }
            }
        }

        return allocation;
    }

    static void optimizeByPotentials(int[][] startingPlan, int[][] cost) {
        System.out.println("\nНачинается рассчёт потенциалов ");
        int m = startingPlan.length;    //количество строк в опорном плане
        int n = startingPlan[0].length; //количество столбцов в опорном плане

        while (true) {
            Integer[] u = new Integer[m];
            Integer[] v = new Integer[n];
            u[0] = 0;

            System.out.println("Формируется набор базисных ячеек");
            List<int[]> basis = new ArrayList<>();
            for (int i = 0; i < m; i++)
                for (int j = 0; j < n; j++)
                    if (startingPlan[i][j] != -1)
                        basis.add(new int[]{i, j});


            System.out.println("Начато вычисление потенциалов строк и столбцов");
            boolean changed;
            do {
                changed = false;
                for (int[] cell : basis) {
                    int i = cell[0], j = cell[1];
                    if (u[i] != null && v[j] == null) {
                        v[j] = cost[i][j] - u[i];
                        changed = true;
                    } else if (u[i] == null && v[j] != null) {
                        u[i] = cost[i][j] - v[j];
                        changed = true;
                    }
                }
            } while (changed);
            System.out.print("\nПотенциалы строк u:");
            printIntegerArray(u);
            System.out.print("Потенциалы столбцов v: ");
            printIntegerArray(v);


            System.out.println("\nНачинается расчёт потенциалов клеток, которые не вошли в базис");
            int minDelta = 0;
            int minI = -1, minJ = -1;
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (startingPlan[i][j] == -1) {
                        int delta = cost[i][j] - u[i] - v[j];
                        if (delta < minDelta) {
                            minDelta = delta;
                            minI = i;
                            minJ = j;
                        }
                    }
                }
            }
            System.out.println("\nМатрица потенциалов (* - базисные клетки):");
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (startingPlan[i][j] == -1) {
                        int delta = cost[i][j] - u[i] - v[j];
                        System.out.printf("%4d", delta);
                    } else {
                        System.out.print("   *");
                    }
                }
                System.out.println();
            }

            if (minDelta >= 0) {
                System.out.println("\nВ итоге после оптимизации план выглядит так:");
                printStartingPlan(startingPlan);

                System.out.println("\nОтрицательных потенциалов нет - опорный план оптимален.");
                printSolution(startingPlan, cost);

                return;
            } else System.out.println("\nОпорный план неоптимален - есть отрицательные потенциалы");

            System.out.println("\nПлан оптимизируется по ячейке (" + minI + "," + minJ + "), Δ = " + minDelta);


            System.out.println("Начинается формирование цикла оптимизации");
            List<Point> cycle = findCycle(startingPlan, minI, minJ);
            if (cycle == null) {
                System.out.println("Не удалось найти цикл.");
                return;
            }

            System.out.println("Цикл оказался найден");
            printCycle(cycle);



            // Находим минимум среди "минусовых" клеток в цикле
            int minValue = Integer.MAX_VALUE;
            for (int k = 1; k < cycle.size(); k += 2) {
                Point p = cycle.get(k); // Минусовые позиции
                minValue = Math.min(minValue, startingPlan[p.row][p.col]);
            }

            // Перераспределим перевозки по циклу
            for (int k = 0; k < cycle.size(); k++) {
                Point p = cycle.get(k);
                if (k % 2 == 0) { // "Плюс" — добавляем значение
                    startingPlan[p.row][p.col] = startingPlan[p.row][p.col] == -1 ? minValue : startingPlan[p.row][p.col] + minValue;
                } else { // "Минус" — вычитаем значение
                    startingPlan[p.row][p.col] -= minValue;
                    if (startingPlan[p.row][p.col] == 0)
                        startingPlan[p.row][p.col] = -1; // Клетка больше не в базисе
                }
            }

            // Добавляем начальную клетку в базис, если она ещё не там
            Point enter = cycle.get(0);
            startingPlan[enter.row][enter.col] = minValue;

            System.out.println("\nНовый опорный план с перераспределёнными потенциалами:");
            printStartingPlan(startingPlan);
            System.out.println("--------------------------------------------------------");

        }
    }


    // Поиск прямоугольного допустимого цикла для ячейки (startRow, startCol)
    static List<Point> findCycle(int[][] startingPlan, int startRow, int startCol) {
        int m = startingPlan.length;
        int n = startingPlan[0].length;

        List<Point> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        path.add(new Point(startRow, startCol));
        return dfsCycle(startingPlan, startRow, startCol, path, visited, true);
    }



static List<Point> dfsCycle(int[][] allocation, int currentRow, int currentCol,
                            List<Point> path, Set<String> visited, boolean alternateRow) {
    int m = allocation.length;
    int n = allocation[0].length;


    if (path.size() >= 4 &&
            currentRow == path.get(0).row &&
            currentCol == path.get(0).col &&
            (path.size() - 1) % 2 == 0) {
        path.remove(path.size() - 1);
        return new ArrayList<>(path);
    }

    String key = currentRow + "," + currentCol;


        if (visited.contains(key)) {       // Если уже посещали эту клетку в рамках текущего пути — прекращаем, чтобы не зациклиться
            return null;
        }


    visited.add(key);



    if (alternateRow) {
        // Горизонтальный поиск — фиксированная строка, перебираем столбцы
        List<Integer> columns = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            if (j != currentCol &&
                    (allocation[currentRow][j] != -1 ||
                            (currentRow == path.get(0).row && j == path.get(0).col))) {
                columns.add(j);
            }
        }
        // Сортировка по расстоянию до начальной колонки
        columns.sort(Comparator.comparingInt(j -> Math.abs(j - path.get(0).col)));

        for (int j : columns) {
            path.add(new Point(currentRow, j));
            List<Point> result = dfsCycle(allocation, currentRow, j, path, visited, false);
            if (result != null)
                return result;
            path.remove(path.size() - 1);
        }

    } else {
        // Вертикальный поиск — фиксированная колонка, перебираем строки
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            if (i != currentRow &&
                    (allocation[i][currentCol] != -1 ||
                            (i == path.get(0).row && currentCol == path.get(0).col))) {
                rows.add(i);
            }
        }
        // Сортировка по расстоянию до начальной строки
        rows.sort(Comparator.comparingInt(i -> Math.abs(i - path.get(0).row)));

        for (int i : rows) {
            path.add(new Point(i, currentCol));
            List<Point> result = dfsCycle(allocation, i, currentCol, path, visited, true);
            if (result != null)
                return result;
            path.remove(path.size() - 1);
        }
    }

    visited.remove(key);
    return null;
}





    static void printStartingPlan(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row)
                System.out.printf("%5s", val == -1 ? "-" : val);
            System.out.println();
        }
    }

    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }
    }

    static void printSolution(int[][] allocation, int[][] cost) {
        int totalCost = 0;
        StringBuilder expression = new StringBuilder();

        for (int i = 0; i < allocation.length; i++) {
            for (int j = 0; j < allocation[0].length; j++) {
                if (allocation[i][j] != -1) {
                    int product = allocation[i][j] * cost[i][j];
                    totalCost += product;
                    if (expression.length() > 0) {
                        expression.append(" + ");
                    }
                    expression.append(allocation[i][j]).append("*").append(cost[i][j]);
                }
            }
        }
        System.out.println("Матрица тарифов:");
        printMatrix(cost);

        System.out.print("\nМинимальная стоимость перевозок F: ");
        System.out.println(expression + " = " + totalCost);
    }


    public static void printIntegerArray(Integer[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            if (i < array.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println();
    }

    // Представляет точку на сетке
    static class Point {
        int row, col;

        Point(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    static void printCycle(List<Point> points) {
        String output = points.stream()
                .map(p -> "(" + p.row + "," + p.col + ")")
                .collect(Collectors.joining(" "));
        System.out.println(output);
    }
}
