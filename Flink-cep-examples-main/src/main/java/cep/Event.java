package cep;

import java.util.HashMap;
import java.util.Map;

public class Event {

    // Mappa che rappresenta i campi dell'evento e i loro valori
    private Map<String, Object> fields;

    // Costruttore che accetta i valori dell'evento
    public Event(String ip, boolean login) {
        fields = new HashMap<>();
        fields.put("ip", ip);       // Campo 'ip'
        fields.put("login", login); // Campo 'login'
    }

    // Metodo per ottenere il valore di un campo specifico
    public Object getField(String key) {
        return fields.get(key);  // Restituisce il valore del campo corrispondente al nome 'key'
    }

    // Metodo per aggiungere altri campi se necessario
    public void addField(String key, Object value) {
        fields.put(key, value);  // Aggiunge un nuovo campo o aggiorna un campo esistente
    }

    // Metodo toString per il debug o per la visualizzazione
    @Override
    public String toString() {
        return "Event{" +
                "fields=" + fields +
                '}';
    }
}
