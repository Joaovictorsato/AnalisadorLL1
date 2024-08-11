import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class LL1_Parser {
    private String input = "";
    private int indexOfInput = -1;
    private Stack<String> stack = new Stack<>();
    private Map<String, Map<String, String>> parseTable = new HashMap<>();
    private String[] nonTers = {"S", "A", "B"};
    private String[] terminals = {"a", "b", "c", "$"};

    public LL1_Parser(String inputFile, String tableFile) throws IOException {
        this.input = readFile(inputFile);
        this.readTable(tableFile);
    }

    private String readFile(String inputFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    private void readTable(String tableFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(tableFile));
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length < 3) {
                error("Formato da tabela inválido na linha: " + line);
            }
            String nonTerminal = parts[0];
            String terminal = parts[1];
            String production = parts[2];

            parseTable
                .computeIfAbsent(nonTerminal, k -> new HashMap<>())
                .put(terminal, production);
        }
        br.close();
    }

    private void pushRule(String rule) {
        for (int i = rule.length() - 1; i >= 0; i--) {
            char ch = rule.charAt(i);
            String str = String.valueOf(ch);
            push(str);
        }
    }

    public void algorithm() {
        push("$");
        push("S");

        String token = read();
        String top;

        do {
            top = this.pop();
            if (isNonTerminal(top)) {
                String rule = this.getRule(top, token);
                this.pushRule(rule);
            } else if (isTerminal(top)) {
                if (!top.equals(token)) {
                    error("Token incorreto: esperado '" + top + "', encontrado '" + token + "'");
                } else {
                    System.out.println("Correspondência: Terminal (" + token + ")");
                    token = read();
                }
            } else {
                error("Erro inesperado com o topo: " + top);
            }

            if (token.equals("$")) {
                break;
            }
        } while (true);

        if (token.equals("$")) {
            System.out.println("Entrada aceita pelo LL1");
        } else {
            System.out.println("Entrada não aceita pelo LL1");
        }
    }

    private boolean isTerminal(String s) {
        for (String terminal : this.terminals) {
            if (s.equals(terminal)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNonTerminal(String s) {
        for (String nonTer : this.nonTers) {
            if (s.equals(nonTer)) {
                return true;
            }
        }
        return false;
    }

    private String read() {
        indexOfInput++;
        if (indexOfInput >= input.length()) {
            error("Fim de entrada inesperado.");
        }
        char ch = this.input.charAt(indexOfInput);
        return String.valueOf(ch);
    }

    private void push(String s) {
        this.stack.push(s);
    }

    private String pop() {
        if (this.stack.isEmpty()) {
            error("Pilha vazia ao tentar desempilhar.");
        }
        return this.stack.pop();
    }

    private void error(String message) {
        System.out.println(message);
        throw new RuntimeException(message);
    }

    public String getRule(String non, String term) {
        Map<String, String> row = parseTable.get(non);
        if (row == null) {
            error(non + " não é um Não-terminal válido");
        }
        String rule = row.get(term);
        if (rule == null) {
            error("Não há regra para: Não-terminal (" + non + "), Terminal (" + term + ")");
        }
        return rule;
    }

    public static void main(String[] args) {
        try {
            LL1_Parser parser = new LL1_Parser("entrada.txt", "tabela.csv");
            parser.algorithm();
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivos: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Erro durante a análise: " + e.getMessage());
        }
    }
}
