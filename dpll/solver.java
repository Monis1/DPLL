import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class Solver {

    public static void main(String[] args) {
        DPLL dpll = new DPLL();
        try {
            dpll.parse_dimacs(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (dpll.execute()) {
            System.out.println("sat");
            System.exit(10);
        } else {
            System.out.println("unsat");
            System.exit(20);
        }
    }
}

class DPLL {

    private Stack<Assignment> trail = new Stack<>();
    private Map<Integer, Boolean> decisions = new HashMap<>();
    private ArrayList<ArrayList<Integer>> clauses = new ArrayList<>();
    private int numberOfVariables;

    void parse_dimacs(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("c")) continue;
            if (line.startsWith("p")) continue;
            ArrayList<Integer> clause = new ArrayList<>();
            for (String literal : line.split("\\s+")) {
                Integer lit = Integer.valueOf(literal);
                if (lit == 0) break;
                clause.add(lit);
                if (Math.abs(lit) > numberOfVariables)
                    numberOfVariables = Math.abs(lit);
            }
            if (clause.size() > 0)
                clauses.add(clause);
        }
    }

    boolean execute() {
        trail.clear();
        if (!booleanContraintPropogation())
            return false;
        while (true) {
            if (!decide())
                return true;
            while (!booleanContraintPropogation())
                if (!backtrack())
                    return false;
        }
    }

    private boolean backtrack() {
        while (true) {
            if (trail.empty())
                return false;
            Assignment assignment = trail.pop();
            decisions.remove(assignment.variable);
            if (!assignment.flipped) {
                trail.push(new Assignment(assignment.variable, !assignment.value, true));
                decisions.put(assignment.variable, !assignment.value);
                return true;
            }
        }
    }

    private boolean decide() {
        if (decisions.size() == numberOfVariables)
            return false;
        for (int i = 1; i <= numberOfVariables; i++) {
            if (!decisions.containsKey(i)) {
                trail.push(new Assignment(i, false, false));
                decisions.put(i, false);
                break;
            }
        }
        return true;
    }

    private boolean booleanContraintPropogation() {
        for (ArrayList<Integer> clause : clauses) {
            if (clause.size() == 1) {
                trail.push(new Assignment(Math.abs(clause.get(0)), clause.get(0) > 0, true));
                decisions.put(Math.abs(clause.get(0)), clause.get(0) > 0);
            }
            checkForUnitClauseAndAssign(clause);
        }
        return !isUnsatisfiedClausePresent();
    }

    private void checkForUnitClauseAndAssign(ArrayList<Integer> clause) {
        int unassignedLiteral = 0;
        int falseLiteralsCount = 0;
        for (int literal : clause) {
            int variable = Math.abs(literal);
            if (decisions.containsKey(variable)) {
                if (literal < 0 ? !decisions.get(variable) : decisions.get(variable))
                    return;
                else
                    falseLiteralsCount++;
            } else if (!decisions.containsKey(variable) && unassignedLiteral != 0)
                return;
            else if (!decisions.containsKey(variable))
                unassignedLiteral = literal;
        }
        if (falseLiteralsCount == clause.size())
            return;
        trail.push(new Assignment(Math.abs(unassignedLiteral), unassignedLiteral > 0, true));
        decisions.put(Math.abs(unassignedLiteral), unassignedLiteral > 0);
    }

    private boolean isUnsatisfiedClausePresent() {
        int fineClausesCount = 0;
        for (ArrayList<Integer> clause : clauses) {
            for (int literal : clause) {
                int variable = Math.abs(literal);
                if (!decisions.containsKey(variable)
                        || (decisions.containsKey(variable) && literal < 0 ? !decisions.get(variable) : decisions.get(variable))) {
                    fineClausesCount++;
                    break;
                }
            }
        }
        return fineClausesCount != clauses.size();
    }

}

class Assignment {

    int variable;
    boolean value;
    boolean flipped;

    Assignment(int variable, boolean value, boolean flipped) {
        this.variable = variable;
        this.value = value;
        this.flipped = flipped;
    }
}
