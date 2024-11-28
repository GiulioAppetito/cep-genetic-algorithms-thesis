package events;

import java.util.HashMap;
import java.util.Map;

public class GenericEvent extends BaseEvent {
    public GenericEvent(Long timestamp) {
        super(timestamp);
    }

    @Override
    public Map<String, Object> toMap() {
        // Crea una nuova mappa modificabile e copia gli attributi esistenti
        Map<String, Object> map = new HashMap<>(super.getAttributes());
        // Aggiungi il timestamp
        map.put("timestamp", timestamp);
        return map; // Ritorna la nuova mappa
    }


    @Override
    public String toString() {
        return "GenericEvent{" +
                "timestamp=" + timestamp +
                ", attributes=" + attributes +
                '}';
    }
}
