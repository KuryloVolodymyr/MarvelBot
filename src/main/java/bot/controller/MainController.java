package bot.controller;

import bot.dto.RequestDTO.Entry;
import bot.dto.RequestDTO.Messaging;
import bot.dto.RequestDTO.RequestData;
import bot.service.ApiCaller;
import bot.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/message")
public class MainController {
    private final String appSecret;
    private final String verifyToken;
    private final String pageAccessToken;

    @Autowired
    MessageService messageService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ApiCaller apiCaller;


    public MainController(@Value("${appSecret}") final String appSecret,
                          @Value("${verifyToken}") final String verifyToken,
                          @Value("${pageAccessToken}") final String pageAccessToken) {
        this.appSecret = appSecret;
        this.verifyToken = verifyToken;
        this.pageAccessToken = pageAccessToken;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> verifyWebhook(@RequestParam("hub.verify_token") String token,
                                                @RequestParam("hub.challenge") String challenge,
                                                @RequestParam("hub.mode") String mode) {
        System.out.println("Challenge");
        if (token.equals(verifyToken)) {
            return new ResponseEntity<>(challenge, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> conversation(@RequestBody RequestData data) {
        for (Entry entry : data.getEntry()) {
            for (Messaging request : entry.getMessaging()) {
                messageService.processRequest(request);
            }
        }

//        apiCaller.broadcastAPI(data.getEntry().get(0).getMessaging().get(0).getMessage().getText());


        return new ResponseEntity<>(HttpStatus.OK);
    }

}
