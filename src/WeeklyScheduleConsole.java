import java.time.LocalDate;
import java.util.*;

public class WeeklyScheduleConsole {

    enum ShiftType { B, L, D }

    static class Employee {
        String name;
        boolean isHeadWaiter;
        int shiftsAssigned = 0;
        Map<LocalDate, List<ShiftType>> assignedShifts = new HashMap<>();

        Employee(String name, boolean isHeadWaiter) {
            this.name = name;
            this.isHeadWaiter = isHeadWaiter;
        }

        void addShift(LocalDate date, ShiftType shift) {
            assignedShifts.computeIfAbsent(date, d -> new ArrayList<>()).add(shift);
            shiftsAssigned++;
        }

        String getShiftString(LocalDate date) {
            List<ShiftType> shifts = assignedShifts.get(date);
            if (shifts == null || shifts.isEmpty()) return "OFF";
            StringBuilder sb = new StringBuilder();
            for (ShiftType s : ShiftType.values()) {
                if (shifts.contains(s)) sb.append(s);
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Lista zaposlenih (uvek ista)
        List<Employee> employees = Arrays.asList(
                new Employee("Alfonso", true),
                new Employee("Victor", true),
                new Employee("Max", true),
                new Employee("Kate", true),
                new Employee("Nikita", false),
                new Employee("Anna", false),
                new Employee("Brooke", false),
                new Employee("Amelia", false),
                new Employee("Dogan", false),
                new Employee("Mihajlo", false),
                new Employee("Dusan", false),
                new Employee("Janja", false),
                new Employee("Mateja M", false),
                new Employee("Lity", false),
                new Employee("Cooper", false),
                new Employee("Jameson", false),
                new Employee("Laci", false),
                new Employee("Isabella", false),
                new Employee("Gianna", false),
                new Employee("Gio", false),
                new Employee("Addison", false),
                new Employee("Cameron", false),
                new Employee("Jane", false),
                new Employee("Roman", false)
        );

        // Unos početnog i krajnjeg datuma
        System.out.print("Unesi datum početka (YYYY-MM-DD): ");
        LocalDate startDate = LocalDate.parse(sc.nextLine());
        System.out.print("Unesi datum kraja (YYYY-MM-DD): ");
        LocalDate endDate = LocalDate.parse(sc.nextLine());

        // Unos broja potrebnih ljudi po smeni
        Map<LocalDate, Map<ShiftType, Integer>> requiredStaff = new LinkedHashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<ShiftType, Integer> shifts = new EnumMap<>(ShiftType.class);
            for (ShiftType shift : ShiftType.values()) {
                System.out.print("Unesi broj potrebnih ljudi za " + date + " (" + shift + "): ");
                shifts.put(shift, sc.nextInt());
            }
            requiredStaff.put(date, shifts);
        }

        // Generisanje rasporeda
        generateSchedule(employees, requiredStaff);

        // Ispis u konzoli
        printSchedule(employees, requiredStaff);
    }

    private static void generateSchedule(List<Employee> employees, Map<LocalDate, Map<ShiftType, Integer>> requiredStaff) {
        int MAX_HEAD = 15;
        int MAX_OTHER = 12;

        List<Employee> headWaiters = new ArrayList<>();
        List<Employee> others = new ArrayList<>();
        for (Employee e : employees) {
            if (e.isHeadWaiter) headWaiters.add(e);
            else others.add(e);
        }

        for (LocalDate day : requiredStaff.keySet()) {
            int totalNeeded = requiredStaff.get(day).values().stream().mapToInt(i -> i).sum();
            while (totalNeeded >= 3) {
                Employee e = pickNextAvailable(headWaiters, others, MAX_HEAD, MAX_OTHER);
                if (e == null) break;
                e.addShift(day, ShiftType.B);
                e.addShift(day, ShiftType.L);
                e.addShift(day, ShiftType.D);
                for (ShiftType st : ShiftType.values()) {
                    requiredStaff.get(day).put(st, requiredStaff.get(day).get(st) - 1);
                }
                totalNeeded -= 3;
            }

            for (ShiftType shift : ShiftType.values()) {
                int needed = requiredStaff.get(day).get(shift);
                if (needed <= 0) continue;

                if (needed > 0) {
                    Employee chosenHead = pickWithLeastShifts(headWaiters, MAX_HEAD, day, shift);
                    if (chosenHead != null) {
                        chosenHead.addShift(day, shift);
                        needed--;
                    }
                }

                while (needed > 0) {
                    Employee next = pickNextAvailable(headWaiters, others, MAX_HEAD, MAX_OTHER);
                    if (next != null) {
                        next.addShift(day, shift);
                        needed--;
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private static Employee pickWithLeastShifts(List<Employee> list, int maxShifts, LocalDate day, ShiftType shift) {
        return list.stream()
                .filter(e -> e.shiftsAssigned < maxShifts && !e.assignedShifts.getOrDefault(day, Collections.emptyList()).contains(shift))
                .min(Comparator.comparingInt(e -> e.shiftsAssigned))
                .orElse(null);
    }

    private static Employee pickNextAvailable(List<Employee> headWaiters, List<Employee> others, int maxHead, int maxOther) {
        List<Employee> combined = new ArrayList<>();
        combined.addAll(headWaiters);
        combined.addAll(others);
        return combined.stream()
                .filter(e -> (e.isHeadWaiter && e.shiftsAssigned < maxHead) || (!e.isHeadWaiter && e.shiftsAssigned < maxOther))
                .min(Comparator.comparingInt(e -> e.shiftsAssigned))
                .orElse(null);
    }

    private static void printSchedule(List<Employee> employees, Map<LocalDate, Map<ShiftType, Integer>> requiredStaff) {
        System.out.printf("%-15s", "Ime");
        for (LocalDate day : requiredStaff.keySet()) {
            System.out.printf("%-10s", day);
        }
        System.out.println();

        for (Employee e : employees) {
            System.out.printf("%-15s", e.name);
            for (LocalDate day : requiredStaff.keySet()) {
                System.out.printf("%-10s", e.getShiftString(day));
            }
            System.out.println();
        }
    }
}
