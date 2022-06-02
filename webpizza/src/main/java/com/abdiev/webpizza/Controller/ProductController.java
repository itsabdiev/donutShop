package com.abdiev.webpizza.Controller;


import com.abdiev.webpizza.Entity.Product;
import com.abdiev.webpizza.Service.ProductService;
import com.abdiev.webpizza.appuser.AppUser;
import com.abdiev.webpizza.appuser.AppUserService;
import com.abdiev.webpizza.registration.RegistrationRequest;
import com.abdiev.webpizza.registration.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;


@AllArgsConstructor
@Controller
public class ProductController {
    private ProductService productService;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private AppUserService appUserService;
    private ArrayList<Long> shopcart = new ArrayList<>();
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping(path = "/login")
    public String login(HttpServletRequest request, Model model) {
        shopcart = new ArrayList<>();
        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            AuthenticationException ex = (AuthenticationException) session
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                errorMessage = ex.getMessage();
            }
        }
        model.addAttribute("errorMessage", errorMessage);
        return "login";
    }



    @PostMapping("/coffix/addtocart/{id}")
    public String addToCart(@PathVariable("id") Long id){
            if (!shopcart.contains(id)) {
                shopcart.add(id);
            }
            System.out.println(shopcart);
        return String.format("redirect:/coffix?product=%d",id);
    }
    @DeleteMapping ("/coffix/removecart/{id}")
    public String deleteCart(@PathVariable("id") Long id){
        shopcart.remove(productService.getProductById(id).getId());
        return "redirect:/coffix/cart";
    }

    @GetMapping("/coffix/cart")
    public ModelAndView cart() {
        ModelAndView modelAndView = new ModelAndView("cart");
        modelAndView.addObject("cart",productService.getProducts().stream().filter(x -> shopcart.contains(x.getId())).collect(Collectors.toList()));
        System.out.println(shopcart);
        return modelAndView;
    }

    @GetMapping(path = "/registerForm")
    public String loginForm(Model model) {
        model.addAttribute("user",new AppUser());
        return "registerForm";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegistrationRequest registrationRequest, Model model) {
        registrationService.register(registrationRequest);
        model.addAttribute("user",registrationRequest.getEmail());
        return "success";
    }
    @GetMapping(path = "/confirm")
    public String confirm(@RequestParam("token") String token) {
        registrationService.confirmToken(token);
        return "confirm";
    }
    @GetMapping({"/main","/"})
    public String getMain(){
        return "main";
    }
    @GetMapping("/coffix")
    public String getProducts(Model model,@RequestParam(value = "product",required = false) Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("list",productService.getProducts());
        model.addAttribute("user",appUserService.getByName(authentication.getName()).get());
        model.addAttribute("quant", (int) productService.getProducts().stream().filter(x -> shopcart.contains(x.getId())).count());
        if (id != null) {
            model.addAttribute("product",productService.getProductById(id));
            System.out.println(productService.getProductById(id));
        }
        return "product";
    }

    @GetMapping("/coffix/addform")
    public String saveProductForm(Model model){
        model.addAttribute("product",new Product());
        return "productadd";
    }

    @PostMapping("/coffix/addproduct")
    public String saveProduct(@ModelAttribute Product product){
        productService.saveProduct(product);
        return "redirect:/coffix";
    }


    @GetMapping("/coffix/updateform/{id}")
    public String updateProductForm(@PathVariable("id") Long id,Model model){
        model.addAttribute("product",productService.getProductById(id));
        return "productupdate";
    }

    @PatchMapping("/coffix/updateproduct/{id}")
    public String updateProduct(@PathVariable("id") Long id,@ModelAttribute Product product){
        productService.updateProduct(id,product);
        return "redirect:/coffix";
    }

    @DeleteMapping("/coffix/deleteproduct/{id}")
    public String updateProduct(@PathVariable("id") Long id){
        productService.deleteProduct(id);
        return "redirect:/coffix";
    }
    public ProductService getProductService() {
        return productService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
