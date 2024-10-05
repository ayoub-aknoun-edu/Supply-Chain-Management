package com.hashing.backend.controller;

import com.hashing.backend.model.Product;
import com.hashing.backend.model.User;
import com.hashing.backend.service.ProductService;
import com.hashing.backend.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final UserService userService;

    public ProductController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProduct(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("receiver")  String receiver_name,
                                                 Authentication authentication) throws Exception {
        User sender = userService.findByUsername(authentication.getName());
        User receiver = userService.findByUsername(receiver_name);
        Product product = productService.createProduct(file, sender, sender, receiver);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{product_id}/verify/{signer_username}")
    public ResponseEntity<Boolean> verifyProduct(@PathVariable Long product_id, @PathVariable String signer_username ) throws Exception {
        Product product = productService.findById(product_id);
        User signer = userService.findByUsername(signer_username);
        boolean isVerified = productService.verifyProduct(product, signer.getId());
        return ResponseEntity.ok(isVerified);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/sender")
    public ResponseEntity<List<Product>> getProductsBySender(Authentication authentication) {
        User sender = userService.findByUsername(authentication.getName());
        List<Product> products = productService.findBySender(sender);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/receiver")
    public ResponseEntity<List<Product>> getProductsByReceiver(Authentication authentication) {
        User receiver = userService.findByUsername(authentication.getName());
        List<Product> products = productService.findByReceiver(receiver);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        Product updatedProduct = productService.updateProduct(product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/download/{productId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long productId) {
        try {
            Resource resource = productService.downloadFile(productId);
            ResponseEntity<Resource> file = ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

            productService.changeFileStatusToSigned(productId);
            return file;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
