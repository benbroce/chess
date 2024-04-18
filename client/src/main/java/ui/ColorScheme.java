package ui;

import static ui.EscapeSequences.*;

public class ColorScheme {
    // Text User Interface
    public static final String SET_PROMPT_COLOR = RESET_BG_COLOR + SET_TEXT_COLOR_WHITE;
    public static final String SET_HELP_OPTION_COLOR = RESET_BG_COLOR + SET_TEXT_COLOR_BLUE;
    public static final String SET_HELP_DESCRIPTOR_COLOR = RESET_BG_COLOR + SET_TEXT_COLOR_MAGENTA;
    public static final String SET_USER_INPUT_COLOR = RESET_BG_COLOR + SET_TEXT_COLOR_GREEN;
    public static final String SET_RESULT_COLOR = RESET_BG_COLOR + SET_TEXT_COLOR_BLUE;
    public static final String SET_NOTIFICATION_COLOR = RESET_BG_COLOR + SET_TEXT_COLOR_YELLOW;
    public static final String SET_ERROR_COLOR = RESET_BG_COLOR + SET_TEXT_COLOR_RED;

    // Chess Board
    public static final String SET_GAME_NAME_COLOR = SET_PROMPT_COLOR;
    public static final String SET_WHITE_USERNAME_COLOR = SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK;
    public static final String SET_BLACK_USERNAME_COLOR = SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE;
    public static final String SET_GRID_LIGHT_COLOR = SET_BG_COLOR_LIGHT_GREY;
    public static final String SET_GRID_DARK_COLOR = SET_BG_COLOR_DARK_GREY;
    public static final String SET_WHITE_PIECE_COLOR = SET_TEXT_COLOR_WHITE;
    public static final String SET_BLACK_PIECE_COLOR = SET_TEXT_COLOR_BLACK;
}
