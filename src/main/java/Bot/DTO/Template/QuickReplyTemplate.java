package Bot.DTO.Template;

import Bot.DTO.Message.QuickReplyMessage;
import Bot.DTO.Recipient;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QuickReplyTemplate implements MessageTemplate {
    private Recipient recipient;
    @JsonProperty("messaging_type")
    private String messagingType;
    private QuickReplyMessage message;

    public QuickReplyTemplate(Recipient recipient, QuickReplyMessage quickReplyMessage){
        this.recipient = recipient;
        this.messagingType = "RESPONSE";
        this.message = quickReplyMessage;
    }

    public QuickReplyTemplate(long id, QuickReplyMessage quickReplyMessage){
        this.recipient = new Recipient(id);
        this.message = quickReplyMessage;
    }
    public QuickReplyTemplate(){
        super();
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public QuickReplyMessage getMessage() {
        return message;
    }

    public void setMessage(QuickReplyMessage message) {
        this.message = message;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }
}
