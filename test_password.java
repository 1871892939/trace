import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String encoded = encoder.encode(password);
        
        System.out.println("密码：" + password);
        System.out.println("BCrypt 加密后：" + encoded);
        System.out.println("\n验证：" + encoder.matches(password, encoded));
        
        // 生成几个备用哈希
        System.out.println("\n备用哈希值（都是 123456）：");
        for (int i = 0; i < 3; i++) {
            System.out.println(encoder.encode(password));
        }
    }
}
