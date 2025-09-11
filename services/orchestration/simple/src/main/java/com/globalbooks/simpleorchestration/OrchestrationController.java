package main.java.com.globalbooks.simpleorchestration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestration")
public class OrchestrationController {

    @Autowired
    private PlaceOrderOrchestrationService orchestrationService;

    @PostMapping("/place-order")
    public ResponseEntity<OrchestrationResponse> placeOrder(@RequestBody OrchestrationRequest request) {
        try {
            OrchestrationResponse response = orchestrationService.placeOrder(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OrchestrationResponse errorResponse = new OrchestrationResponse();
            errorResponse.setStatus("SYSTEM_ERROR");
            errorResponse.setErrorMessage(e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Orchestration Service is running");
    }
}