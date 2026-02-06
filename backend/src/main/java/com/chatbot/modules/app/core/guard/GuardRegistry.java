package com.chatbot.modules.app.core.guard;

import com.chatbot.modules.app.core.model.AppCode;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Component
/**
 * GuardRegistry
 *
 * - Register all AppServiceGuard
 * - Route guard by AppCode
 */
public class GuardRegistry {

    private final Map<AppCode, List<AppServiceGuard>> guardMap;

    public GuardRegistry(List<AppServiceGuard> guards) {
        this.guardMap = guards.stream()
                .flatMap(guard ->
                        guardSupportedCodes(guard).stream()
                                .map(code -> Map.entry(code, guard))
                )
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    /**
     * Get guards by AppCode
     */
    public List<AppServiceGuard> getGuards(AppCode appCode) {
        return guardMap.getOrDefault(appCode, List.of());
    }

    /**
     * Extract supported AppCode(s) from guard
     */
    private List<AppCode> guardSupportedCodes(AppServiceGuard guard) {
        return List.of(AppCode.values()).stream()
                .filter(guard::supports)
                .collect(Collectors.toList());
    }
}
