/**
 * @author SzeYing
 * @since 2017-02-17
 */
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * @author SzeYing
 * @since 2017-02-17
 */
public class Main {

    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new SchedulerBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}