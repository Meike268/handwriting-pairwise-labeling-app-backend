import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/test")

class TestController {
    @GetMapping
    fun test(): String {
        return "Hello, world!"
    }
}
