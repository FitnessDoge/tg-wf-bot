package tgbot.tgwfbot.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class WeatherForecastBot implements LongPollingSingleThreadUpdateConsumer, SpringLongPollingBot {
    private final TelegramClient client;

    public WeatherForecastBot() {
        client = new OkHttpTelegramClient(getBotToken());
    }

    @Override
    public String getBotToken() {
        return System.getenv("TG_BOT_TOKEN");
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        Message message;
        if (update.hasMessage()) {
            message = update.getMessage();
        } else if (update.hasCallbackQuery()) {
            // TODO: handle callback
            return;
        } else {
            return;
        }

        User from = message.getFrom();
        Long chatId = message.getChatId();
        System.out.println("Message:[" + message.getText() + "] from =>" + from);

        if (message.isCommand()) {
            String command = message.getText();
            if ("/start".equals(command)) {
                String answerMessage = "Hello,there is Techjoy bot. I'll give you the weather forecast.";
                sendSimpleMessage(chatId, answerMessage);
            } else if ("/forecast".equals(command)) {
                // TODO Forecast for tomorrow
            } else if ("/forecast3".equals(command)) {
                // TODO Forecast for the incoming three days
            }
        } else {
            String answerMessage = "Please enter the correct command.";
            sendSimpleMessage(chatId, answerMessage);
        }

    }

    private void sendSimpleMessage(Long chatId, String answerMessage) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(answerMessage)
                .build();
        try {
            client.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
