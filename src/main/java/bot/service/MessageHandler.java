package bot.service;

import bot.dto.DialogFlowDTO.DialogFlowRequest.DialogFlowRequest;
import bot.dto.DialogFlowDTO.DialogFlowResponse.DialogFlowResponse;
import bot.dto.MarvelDTO.CharacterResults;
import bot.dto.MarvelDTO.MarvelCharacterResponse;
import bot.dto.MarvelDTO.MarvelComicsResponse;
import bot.dto.RequestDTO.Messaging;
import bot.dto.Template.MessageTemplate;
import bot.dto.Template.QuickReplyTemplate;
import bot.dto.Template.TextMessageTemplate;
import bot.dto.UserProfile;
import bot.domain.UserRequestEntity;
import bot.domain.UserEntity;
import bot.repository.UserRequestRepository;
import bot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageHandler {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ApiCaller apiCaller;

    @Autowired
    private MarvelTemplateBuilder marvelTemplateBuilder;

    @Autowired
    private UserRequestRepository userRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageTypeDetector messageTypeDetector;


    @Value("${response.greeting}")
    private String greeting;

    @Value("${response.herNotFound}")
    private String heroNotFound;

    @Value("${response.noTemplateInitialized}")
    private String noTemplateInitialized;

    @Value("${response.noComicsFound}")
    private String noComicsFound;

    @Value("${response.cantFindHeroName}")
    private String cantFindHeroName;

    @Value("${response.handleImage}")
    private String imageHandleMessage;

    @Value("${response.httpException}")
    private String httpExceptionMessage;

    @Value("${response.ratingReply}")
    private String ratingReply;

    @Value("${response.help}")
    private String helpMessage;

    @Value("${response.topHeroes}")
    private String topHeroes;

    @Value("${response.settingsChanged}")
    private String settingsChanged;

    public MessageTemplate handleNonTextMessage(Messaging request) {

        long id = request.getSender().getId();
        MessageTemplate template = new TextMessageTemplate(id, noTemplateInitialized);
        if (messageTypeDetector.isImage(request)) {
            template = handleImageMessage(request);
        } else if (messageTypeDetector.isStart(request)) {
            System.out.println("handling start button");
            template = handleGreeting(request);
        } else if (messageTypeDetector.isHelp(request)) {
            template = handleHelpTemplate(request);
        } else if (messageTypeDetector.isTop(request)) {
            template = handleTopTemplate(request);
        } else if (messageTypeDetector.isChangeComicsAmound(request)) {
            template = handleChangeComicsAtOnce(request);
        } else if (messageTypeDetector.isGetComics(request)) {
            template = handleComicsTemplate(request);
        } else if (messageTypeDetector.isMoreComics(request)) {
            template = handleMoreComics(request);
        } else if (messageTypeDetector.isRate(request)) {
            template = handleRatingTemplate(request);
        }
        return template;
    }

    public MessageTemplate handleMessageWithText(Messaging request) {

        long id = request.getSender().getId();
        MessageTemplate template = new TextMessageTemplate(id, noTemplateInitialized);
        if (messageTypeDetector.isQuickReply(request)) {
            if (messageTypeDetector.isHeroQuickReply(request)) {
                template = handleGreetingQuickReply(request);
            } else if (messageTypeDetector.isRatingQuickReply(request)) {
                messageService.rateHero(request);
                template = handleRatingSuccessfullTemplate(request);
            }
        } else {
            template = handleTextMessage(request);
        }
        return template;
    }

    private MessageTemplate handleImageMessage(Messaging request) {
        return new TextMessageTemplate(request.getSender().getId(), imageHandleMessage);
    }

    private MessageTemplate handleGreeting(Messaging request) {
        Long senderPSID = request.getSender().getId();
        if (userRepository.getBySenderPSID(senderPSID) == null) {
            UserProfile userProfile = apiCaller.callGraphApi(senderPSID);
            userRepository.save(new UserEntity(senderPSID, userProfile));
        }
        String greetingResponce = "Hi "+userRepository.getBySenderPSID(senderPSID).getFirstName()+greeting;
        return new QuickReplyTemplate(senderPSID, messageService.getHeroesForQuickReply(greetingResponce));
    }

    private MessageTemplate handleTextMessage(Messaging request) {
        MessageTemplate messageTemplate;
        String textMessage = request.getMessage().getText();

//
        DialogFlowRequest dialogFlowRequest = new DialogFlowRequest(textMessage);
//
        DialogFlowResponse dialogFlowResponse = apiCaller.callDialogFlowApi(dialogFlowRequest);
//
//        if (dialogFlowResponse.getResult().getMetadata().getIntentName() == null) {
            messageTemplate = new TextMessageTemplate(request.getSender().getId(), cantFindHeroName);
//        } else {
//            messageTemplate = handleDialogFlowResponse(dialogFlowResponse, request);
//        }


        return messageTemplate;
    }

    private MessageTemplate handleRatingTemplate(Messaging request) {
        long id = request.getSender().getId();
        return new QuickReplyTemplate(id, messageService.getRatingQuickReply(request.getPostback().getPayload()));
    }

    private MessageTemplate handleRatingSuccessfullTemplate(Messaging request) {
        Long id = request.getSender().getId();
        return new QuickReplyTemplate(id, messageService.getHeroesForQuickReply(ratingReply));
    }

    private MessageTemplate handleComicsTemplate(Messaging request) {

        MessageTemplate template;
//        Long limit = userRepository.getBySenderPSID(request.getSender().getId()).getComicsGivenAtOnce();

        Long limit = 5L;
        MarvelComicsResponse marvelComicsResponse = apiCaller.callMarvelAPIForComics(request.getPostback().getPayload(), limit);
        if (!marvelComicsResponse.getData().getResults().isEmpty()) {
            template = marvelTemplateBuilder.buildGenericTemplateFromMarvelComicsResponce(request, marvelComicsResponse);
        } else {
            template = new TextMessageTemplate(request.getSender().getId(), noComicsFound);
        }

        return template;
    }

    private MessageTemplate handleMoreComics(Messaging request) {
        MessageTemplate template;

        String characterId = request.getPostback().getPayload().split("/")[0];
        Long offsetLong = Long.parseLong(request.getPostback().getPayload().split("/")[1]);

        Long limit = userRepository.getBySenderPSID(request.getSender().getId()).getComicsGivenAtOnce();
        offsetLong += limit;
        String offset = offsetLong.toString();

        MarvelComicsResponse marvelComicsResponse = apiCaller.callMarvelAPIForComics(characterId, limit, offset);
        if (!marvelComicsResponse.getData().getResults().isEmpty()) {
            template = marvelTemplateBuilder.buildGenericTemplateFromMarvelComicsResponce(request, marvelComicsResponse, offset, characterId);
        } else {
            template = new TextMessageTemplate(request.getSender().getId(), noComicsFound);
        }

        return template;
    }

    private MessageTemplate handleGreetingQuickReply(Messaging request) {

        MessageTemplate template;
        String characterName = request.getMessage().getText();

        MarvelCharacterResponse marvelCharacterResponse = apiCaller.callMarvelAPIForCharacter(characterName);

        template = getMarvelCharacter(request, marvelCharacterResponse);

        return template;
    }

    private MessageTemplate handleHelpTemplate(Messaging request) {
        Long recepientId = request.getSender().getId();
        return new TextMessageTemplate(recepientId, helpMessage);
    }

    private MessageTemplate handleTopTemplate(Messaging request) {
        Long recepientId = request.getSender().getId();
        return new QuickReplyTemplate(recepientId, messageService.getHeroesForQuickReply(topHeroes));
    }

    private MessageTemplate handleChangeComicsAtOnce(Messaging request) {
        Long senderPSID = request.getSender().getId();
        Long newComicsNumber = Long.parseLong(request.getPostback().getTitle());
        UserEntity user = userRepository.getBySenderPSID(senderPSID);
        user.setComicsGivenAtOnce(newComicsNumber);
        userRepository.save(user);
        return new TextMessageTemplate(senderPSID, settingsChanged);
    }

    private MessageTemplate handleDialogFlowResponse(DialogFlowResponse dialogFlowResponse, Messaging request) {
        //Checking if type of intent is one than needs special handling
        // if not, handling it in getTemplateForCharacter method
        MessageTemplate messageTemplate;
        String intentType = dialogFlowResponse.getResult().getMetadata().getIntentName();
        switch (intentType) {
            case "thankYou":
                messageTemplate = new TextMessageTemplate(request.getSender().getId(), dialogFlowResponse.getResult().getFulfillment().getSpeech());
                break;
            case "help":
                messageTemplate = new TextMessageTemplate(request.getSender().getId(), helpMessage);
                break;
            case "top":
                messageTemplate = handleTopTemplate(request);
                break;
//todo
            case "comics":
                if (dialogFlowResponse.getResult().getActionIncomplete())
                    messageTemplate = new TextMessageTemplate(request.getSender().getId(), "Please type in name of your hero");
                else {
                    MarvelCharacterResponse marvelCharacterResponse = apiCaller.callMarvelAPIForCharacter(request.getMessage().getText());
                    messageTemplate = new TextMessageTemplate(request.getSender().getId(), "complete");
                }
                break;

            case "dc":
                messageTemplate = new TextMessageTemplate(request.getSender().getId(), dialogFlowResponse.getResult().getFulfillment().getSpeech());
                break;
            default:
                messageTemplate = getTemplateForCharacter(request, dialogFlowResponse);
                break;
        }
        return messageTemplate;
    }

    private MessageTemplate getTemplateForCharacter(Messaging request, DialogFlowResponse dialogFlowResponse) {
        // if parameters field is empty, than we can`t find hero name
        // otherwise calling marvel api with character name found in parameters
        MessageTemplate messageTemplate;
        if (dialogFlowResponse.getResult().getParameters() == null) {
            messageTemplate = new TextMessageTemplate(request.getSender().getId(), cantFindHeroName);
        } else {
            String characterName = dialogFlowResponse.getResult().getParameters().getHeroName();
            MarvelCharacterResponse marvelCharacterResponse = apiCaller.callMarvelAPIForCharacter(characterName);

            messageTemplate = getMarvelCharacter(request, marvelCharacterResponse);
        }
        return messageTemplate;
    }

    private MessageTemplate getMarvelCharacter(Messaging request, MarvelCharacterResponse marvelCharacterResponse) {
        //if marvel API returned some character info we save it to database and pass it to MarvelCharacterResponceBuilder
        //otherwise sending "heroNotFound" message
        MessageTemplate template;
        if (!marvelCharacterResponse.getData().getResults().isEmpty()) {
            Long senderPSID = request.getSender().getId();
            UserEntity user = userRepository.getBySenderPSID(senderPSID);

            //Saving Responce to database
            for (CharacterResults results : marvelCharacterResponse.getData().getResults()) {
                String character = results.getName();
                Long characterId = results.getId();
                UserRequestEntity userRequest = new UserRequestEntity(character, characterId);
//                userRequest.setUser(user);
                userRequestRepository.save(userRequest);
            }
            template = marvelTemplateBuilder.buildGenericTemplateFromMarvelCharacterResponse(request, marvelCharacterResponse);

        } else {
            template = new TextMessageTemplate(request.getSender().getId(),
                    heroNotFound);
        }
        return template;
    }
}
