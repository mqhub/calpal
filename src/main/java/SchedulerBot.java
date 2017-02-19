import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * @author SzeYing
 * @since 2017-02-17
 */
public class SchedulerBot extends TelegramLongPollingBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerBot.class);

    public SchedulerBot() {
    }

    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                LOGGER.info("Got message from {}", update.getMessage().getChatId());

                RequestHandler requestHandler = new RequestHandler(update);
                sendMessage(requestHandler.execute());

            }
        } catch (TelegramApiException tae) {
            LOGGER.error("Could not send message");
        }
    }

    public String getBotUsername() {
        return "ScheduleBuddyBot";
    }

    public String getBotToken() {
        return "";
    }
}
