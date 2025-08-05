package iranga.mg.social.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import iranga.mg.social.repository.OnlineUserRepository;
import iranga.mg.social.repository.UserRepository;
@Controller
public class ViewController {

	@Autowired
	UserRepository userRepo;

	@Autowired
	OnlineUserRepository onlineUserRepo;

	Logger logger = LoggerFactory.getLogger(MessagingController.class);

	@GetMapping("/")
	public String mainPage() {
		return "index.html";
	}

	@GetMapping("/chat")
	public String privateChatView() {
		return "chat.html";
	}

	@GetMapping("/registration")
	public String registration() {
		return "registration.html";
	}

	@GetMapping("/disconnect")
	public ModelAndView disconnect() {
		SecurityContextHolder.clearContext();
		ModelAndView mav = new ModelAndView();
		mav.setViewName("index.html");
		return mav;
	}

	@GetMapping("/create-chat")
	public String createChatView() {
		return "create-chat.html";
	}

}