package br.com.dio;

import br.com.dio.model.Board;
import br.com.dio.model.Space;
import br.com.dio.util.BoardTemplate; // Corrigido o import para a classe BoardTemplate
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import java.util.Scanner;
import static java.util.stream.Collectors.toMap;
import java.util.stream.Stream;

public class Main {

    private final static Scanner scanner = new Scanner(System.in);

    private static Board board;

    private final static int BOARD_LIMIT = 9;

    public static void main(String[] args) {
        final var positions = Stream.of(args)
                .collect(toMap(
                        k -> k.split(";")[0],
                        v -> v.split(";")[1]));
        while (true) {
            System.out.println("Selecione uma das opções a seguir:");
            System.out.println("1 - Iniciar um novo Jogo");
            System.out.println("2 - Colocar um novo número");
            System.out.println("3 - Remover um número");
            System.out.println("4 - Visualizar jogo atual");
            System.out.println("5 - Verificar status do jogo");
            System.out.println("6 - Limpar jogo");
            System.out.println("7 - Finalizar jogo");
            System.out.println("8 - Sair");

            var option = runUntilGetValidNumber(1, 8);

            switch (option) {
                case 1 -> startGame(positions);
                case 2 -> inputNumber();
                case 3 -> removeNumber();
                case 4 -> showCurrentGame();
                case 5 -> showGameStatus();
                case 6 -> clearGame();
                case 7 -> finishGame();
                case 8 -> {
                    System.out.println("Saindo do jogo. Até a próxima!");
                    System.exit(0);
                }
                default -> System.out.println("Opção inválida, selecione uma das opções do menu.");
            }
        }
    }

    private static void startGame(final Map<String, String> positions) {
        if (nonNull(board)) {
            System.out.println("O jogo já foi iniciado");
            return;
        }

        List<List<Space>> spaces = new ArrayList<>();
        for (int i = 0; i < BOARD_LIMIT; i++) {
            spaces.add(new ArrayList<>());
            for (int j = 0; j < BOARD_LIMIT; j++) {
                var key = "%s,%s".formatted(i, j);
                var positionConfig = positions.get(key);

                if (isNull(positionConfig)) {
                    System.out.printf(
                            "Configuração ausente para a posição [%s,%s]. Certifique-se de fornecer todas as posições.\n",
                            i, j);
                    return;
                }

                var expected = Integer.parseInt(positionConfig.split(",")[0]);
                var fixed = Boolean.parseBoolean(positionConfig.split(",")[1]);
                var currentSpace = new Space(expected, fixed);
                spaces.get(i).add(currentSpace);
            }
        }

        board = new Board(spaces);
        System.out.println("O jogo está pronto para começar");
    }

    private static void inputNumber() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }

        System.out.println("Informe a coluna onde o número será inserido:");
        var col = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a linha onde o número será inserido:");
        var row = runUntilGetValidNumber(0, 8);
        System.out.printf("Informe o número que vai entrar na posição [%s,%s]:\n", col, row);
        var value = runUntilGetValidNumber(1, 9);
        if (!board.changeValue(col, row, value)) {
            System.out.printf("A posição [%s,%s] tem um valor fixo e não pode ser alterada.\n", col, row);
        } else {
            System.out.printf("Número %s inserido na posição [%s,%s].\n", value, col, row);
        }
    }

    private static void removeNumber() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }

        System.out.println("Informe a coluna onde o número será removido:");
        var col = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a linha onde o número será removido:");
        var row = runUntilGetValidNumber(0, 8);
        if (!board.clearValue(col, row)) {
            System.out.printf("A posição [%s,%s] tem um valor fixo e não pode ser alterada.\n", col, row);
        } else {
            System.out.printf("Número removido da posição [%s,%s].\n", col, row);
        }
    }

    private static void showCurrentGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }

        var args = new Object[81];
        var argPos = 0;
        for (int i = 0; i < BOARD_LIMIT; i++) {
            for (var col : board.getSpaces()) {
                args[argPos++] = " " + (isNull(col.get(i).getActual()) ? " " : col.get(i).getActual());
            }
        }
        System.out.println("Seu jogo está assim:");
        System.out.printf((BoardTemplate.BOARD_TEMPLATE) + "\n", args);
    }

    private static void showGameStatus() {
        if (isNull(board)) {
            System.out.println(BoardTemplate.RED + "O jogo ainda não foi iniciado." + BoardTemplate.RESET);
            return;
        }

        System.out.printf("O jogo atualmente está no status: %s%s%s\n", BoardTemplate.GREEN,
                board.getStatus().getLabel(), BoardTemplate.RESET);
        if (board.hasErrors()) {
            System.out.println(BoardTemplate.RED + "O jogo contém erros. Verifique e corrija." + BoardTemplate.RESET);
        } else {
            System.out.println(BoardTemplate.GREEN + "O jogo não contém erros." + BoardTemplate.RESET);
        }
    }

    private static void clearGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }

        System.out.println("Tem certeza que deseja limpar seu jogo e perder todo o progresso? (sim/não)");
        var confirm = scanner.next();
        while (!confirm.equalsIgnoreCase("sim") && !confirm.equalsIgnoreCase("não")) {
            System.out.println("Informe 'sim' ou 'não':");
            confirm = scanner.next();
        }

        if (confirm.equalsIgnoreCase("sim")) {
            board.reset();
            System.out.println("O jogo foi limpo.");
        } else {
            System.out.println("Ação cancelada. O jogo não foi limpo.");
        }
    }

    private static void finishGame() {
        if (isNull(board)) {
            System.out.println(BoardTemplate.RED + "O jogo ainda não foi iniciado." + BoardTemplate.RESET);
            return;
        }

        if (board.gameIsFinished()) {
            System.out.println(BoardTemplate.GREEN + "Parabéns! Você concluiu o jogo." + BoardTemplate.RESET);
            showCurrentGame();
            board = null;
        } else if (board.hasErrors()) {
            System.out.println(BoardTemplate.RED + "Seu jogo contém erros. Verifique o tabuleiro e ajuste-o."
                    + BoardTemplate.RESET);
        } else {
            System.out.println(BoardTemplate.RED + "Você ainda precisa preencher algum espaço." + BoardTemplate.RESET);
        }
    }

    private static int runUntilGetValidNumber(final int min, final int max) {
        while (true) {
            try {
                var current = scanner.nextInt();
                if (current >= min && current <= max) {
                    return current;
                }
                System.out.printf("Informe um número entre %s e %s:\n", min, max);
            } catch (Exception e) {
                System.out.println("Entrada inválida. Por favor, insira um número.");
                scanner.next(); // Limpa a entrada inválida
            }
        }
    }

}
