package com.waquarshamsi.api.telegram_notifer.controller;

import com.waquarshamsi.api.telegram_notifer.service.DeadLetterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dlq")
@Tag(name = "DLQ Management", description = "APIs for managing the Dead Letter Queue")
public class DeadLetterController {

    private final DeadLetterService deadLetterService;

    public DeadLetterController(DeadLetterService deadLetterService) {
        this.deadLetterService = deadLetterService;
    }

    @GetMapping("/count")
    @Operation(summary = "Get DLQ message count", description = "Returns the current number of messages in the Dead Letter Queue.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the count")
    })
    public ResponseEntity<Map<String, Long>> getDlqMessageCount() {
        return ResponseEntity.ok(Map.of("messageCount", deadLetterService.getDlqMessageCount()));
    }

    @PostMapping("/reprocess-one")
    @Operation(summary = "Reprocess one message", description = "Takes a single message from the DLQ and sends it back to the main queue for another processing attempt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message successfully re-queued or DLQ was empty")
    })
    public ResponseEntity<Map<String, String>> reprocessOneMessage() {
        boolean reprocessed = deadLetterService.reprocessOneMessage();
        String message = reprocessed
                ? "One message from DLQ has been re-queued for processing."
                : "DLQ is empty. No message to reprocess.";
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/reprocess-all")
    @Operation(summary = "Reprocess all messages", description = "Takes all messages currently in the DLQ and sends them back to the main queue for processing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All messages successfully re-queued")
    })
    public ResponseEntity<Map<String, String>> reprocessAllMessages() {
        long count = deadLetterService.reprocessAllMessages();
        return ResponseEntity.ok(Map.of("message", "Attempted to reprocess " + count + " message(s) from the DLQ."));
    }

    @DeleteMapping("/purge")
    @Operation(summary = "Purge the DLQ", description = "Permanently deletes all messages from the Dead Letter Queue. This action is irreversible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DLQ successfully purged")
    })
    public ResponseEntity<Map<String, String>> purgeDlq() {
        long count = deadLetterService.purgeDlq();
        return ResponseEntity.ok(Map.of("message", "Purged " + count + " message(s) from the DLQ."));
    }
}