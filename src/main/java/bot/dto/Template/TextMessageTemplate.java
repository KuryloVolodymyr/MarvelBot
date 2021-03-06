package bot.dto.Template;

import bot.dto.Message.TextMessage;
import bot.dto.Recipient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class TextMessageTemplate extends MessageTemplate {
    private Recipient recipient;
    @JsonProperty("messaging_type")
    private String messagingType;
    private TextMessage message;

    public TextMessageTemplate() {
    }

    public TextMessageTemplate(long id, String text) {
        this.recipient = new Recipient(id);
        this.messagingType = "RESPONSE";
        this.message = new TextMessage(text);
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public TextMessage getMessage() {
        return message;
    }

    public void setMessage(TextMessage message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return (recipient.toString());
    }
}
