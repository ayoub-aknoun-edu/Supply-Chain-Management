package com.hashing.backend.service;

import com.hashing.backend.model.Product;
import com.hashing.backend.model.User;
import com.hashing.backend.repository.ProductRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import org.apache.commons.codec.binary.Base64;


@Service
public class ProductService {


    private final ProductRepository productRepository;
    private final DigitalSignatureService digitalSignatureService;
    private final UserService userService;
    private Path fileStorageLocation;

    public ProductService(ProductRepository productRepository, DigitalSignatureService digitalSignatureService, UserService userService) {
        this.productRepository = productRepository;
        this.digitalSignatureService = digitalSignatureService;
        this.userService = userService;
        this.fileStorageLocation = Paths.get("../resources/uploaded").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Product createProduct(MultipartFile file, User sender, User signer, User receiver) throws Exception {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        try {
            Files.copy(file.getInputStream(), targetLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store the file. Please try again!", ex);
        }

        byte[] fileContent = file.getBytes();
        String hash = digitalSignatureService.hashFile(fileContent);

        PrivateKey privateKey = getPrivateKeyFromString(signer.getPrivateKey());
        String signature = digitalSignatureService.sign(hash, privateKey);

        Product product = new Product();
        product.setName(file.getOriginalFilename());
        product.setHash(hash);
        product.setSignature(signature);
        product.setSender(sender);
        product.setSigner(signer);
        product.setReceiver(receiver);
        product.setStatus(Product.Status.SIGNED);
        product.setFilePath(targetLocation.toString());
        return productRepository.save(product);
    }

    public Resource downloadFile(Long productId) throws Exception {
        Product product = findById(productId);
        Path filePath = Paths.get(product.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if(resource.exists()) {
            return resource;
        } else {
            throw new RuntimeException("File not found " + product.getName());
        }
    }

    public PrivateKey getPrivateKeyFromString(String key) throws Exception {
        // Decode the Base64 encoded string
        byte[] privateKeyBytes = Base64.decodeBase64(key);
        // Create the key specification
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        // Get a KeyFactory for the RSA algorithm
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        // Generate the private key from the key specification
        return keyFactory.generatePrivate(keySpec);
    }

    public  PublicKey getPublicKeyFromString(String key) throws Exception {
        // Decode the Base64 encoded string
        byte[] publicKeyBytes = Base64.decodeBase64(key);
        // Create the key specification
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        // Get a KeyFactory for the RSA algorithm
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        // Generate the public key from the key specification
        return keyFactory.generatePublic(keySpec);
    }

    public boolean verifyProduct(Product product, Long id) throws Exception {
        User signer = userService.findById(id);
        if (product.getStatus() != Product.Status.SIGNED) {
            throw new RuntimeException("Product is not signed yet");
        }

        // Verify file hash
        Path filePath = Paths.get(product.getFilePath());
        byte[] fileContent = Files.readAllBytes(filePath);
        String currentHash = digitalSignatureService.hashFile(fileContent);

        if (!currentHash.equals(product.getHash())) {
            product.setStatus(Product.Status.INSECURE);
            productRepository.save(product);
            return false;
        }

        // Verify digital signature
        PublicKey publicKey = getPublicKeyFromString(signer.getPublicKey());
        boolean isSignatureVerified;
        try {
            isSignatureVerified = digitalSignatureService.verify(product.getHash(), product.getSignature(), publicKey);
        } catch (Exception e) {
            System.out.println("Unmatched signature");
            product.setStatus(Product.Status.INSECURE);
            productRepository.save(product);
            return false;
        }

        if (isSignatureVerified) {
            product.setStatus(Product.Status.VERIFIED);
            productRepository.save(product);
            return true;
        } else {
            product.setStatus(Product.Status.INSECURE);
            productRepository.save(product);
            return false;
        }
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> findBySender(User sender) {
        return productRepository.findBySender(sender);
    }

    public List<Product> findByReceiver(User receiver) {
        return productRepository.findByReceiver(receiver);
    }

    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public void changeFileStatusToSigned(Long productId) {
        Product product = findById(productId);
        product.setStatus(Product.Status.SIGNED);
        productRepository.save(product);
    }
}
