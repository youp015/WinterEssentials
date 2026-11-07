package nl.winteressentials;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.winteressentials.message.MessageService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {

    @Test
    void testFormatDuration() {
        assertEquals("0s", MessageService.formatDuration(0));
        assertEquals("0s", MessageService.formatDuration(-5));
        assertEquals("30s", MessageService.formatDuration(30));
        assertEquals("1m", MessageService.formatDuration(60));
        assertEquals("1m 15s", MessageService.formatDuration(75));
        assertEquals("1u 2m 5s", MessageService.formatDuration(3725));
    }

    @Test
    void testPlaceholderReplacement() {
        Map<String, String> map = MessageService.expandPlaceholders(MessageService.toMap("home", "spawn", "seconds", "5", "number", "3"));

        String t1 = MessageService.replacePlaceholders("<gray>Teleporteren naar <#80D8FF>{home}</#80D8FF>...", map);
        assertEquals("<gray>Teleporteren naar <#80D8FF>spawn</#80D8FF>...", t1);

        String t2 = MessageService.replacePlaceholders("<#80D8FF>Teleportatie in <white>{seconden}s<#80D8FF>...", map);
        assertEquals("<#80D8FF>Teleportatie in <white>5s<#80D8FF>...", t2);

        String t3 = MessageService.replacePlaceholders("<#80D8FF>Teleportatie in <white>{seconds}s<#80D8FF>...", map);
        assertEquals("<#80D8FF>Teleportatie in <white>5s<#80D8FF>...", t3);

        String t4 = MessageService.replacePlaceholders("<red>Je hebt geen toegang tot vault nummer <white>{number}</white>.", map);
        assertEquals("<red>Je hebt geen toegang tot vault nummer <white>3</white>.", t4);

        String t5 = MessageService.replacePlaceholders("<red>Je hebt geen toegang tot vault nummer <white>{nummer}</white>.", map);
        assertEquals("<red>Je hebt geen toegang tot vault nummer <white>3</white>.", t5);
    }
}
