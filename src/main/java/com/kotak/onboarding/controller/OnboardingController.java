package com.kotak.onboarding.controller;

import com.kotak.onboarding.model.OnboardingRequest;
import com.kotak.onboarding.model.TokenRequest;
import com.kotak.onboarding.model.VideoStatusRequest;
import com.kotak.onboarding.service.MoxoService;
import com.kotak.onboarding.service.RmRoundRobinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin(origins = "*")
public class OnboardingController {

    private final MoxoService moxoService;
    private final RmRoundRobinService rmService;

    public OnboardingController(MoxoService moxoService, RmRoundRobinService rmService) {
        this.moxoService = moxoService;
        this.rmService   = rmService;
    }

    // POST /api/onboarding/initiate
    // Triggers the Moxo webhook with round-robin NRM assignment, returns binderId
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiate(@RequestBody OnboardingRequest req) {
        Map<String, Object> result = new HashMap<>();
        try {
            RmRoundRobinService.Rm rm = rmService.pickNext();
            String fullName = req.getFirstName() + " " + req.getLastName();

            String binderId = moxoService.triggerWebhookAndAssignNrm(
                fullName, req.getEmail(), req.getPhone(), req.getState(),
                rm.email(), rm.name()
            );

            result.put("success", true);
            result.put("binderId", binderId);
            result.put("assignedRm", rm.email());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    // POST /api/onboarding/video-status
    // Updates workspace_variable.video_status — "yes" when RM joins, "no" on double timeout
    @PostMapping("/video-status")
    public ResponseEntity<Map<String, Object>> videoStatus(@RequestBody VideoStatusRequest req) {
        Map<String, Object> result = new HashMap<>();
        try {
            moxoService.updateVideoStatus(req.getBinderId(), req.getStatus());
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    // POST /api/onboarding/token
    // Returns a Moxo access token for the given user email (used to init the SDK)
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> token(@RequestBody TokenRequest req) {
        Map<String, Object> result = new HashMap<>();
        try {
            String accessToken = moxoService.getToken(req.getEmail());
            result.put("success", true);
            result.put("accessToken", accessToken);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
