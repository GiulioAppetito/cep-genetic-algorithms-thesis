package cep.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import events.BaseEvent;
import events.GenericEvent;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.junit.jupiter.api.Test;
import representation.PatternRepresentation;
import representation.mappers.RepresentationToPatternMapper;

import java.time.Duration;
import java.util.List;

public class RepresentationToPatternMapperTest {

    @Test
    public void testPatternConformance() {
        // Step 1: Create a sample PatternRepresentation
        PatternRepresentation representation = createSamplePatternRepresentation();

        // Step 2: Use the mapper to generate the Flink pattern from the representation
        RepresentationToPatternMapper<BaseEvent> mapper = new RepresentationToPatternMapper<>();
        Pattern<BaseEvent, ?> generatedPattern = mapper.convert(representation);

        // Step 3: Construct the expected Flink pattern directly (based on expected structure)
        Pattern<BaseEvent, ?> expectedPattern = createExpectedPattern();

        // Step 4: Compare the patterns
        assertPatternsEqual(expectedPattern, generatedPattern);
    }

    private PatternRepresentation createSamplePatternRepresentation() {
        // Create conditions
        PatternRepresentation.Condition condition1 = new PatternRepresentation.Condition(
                "sensor_id", PatternRepresentation.Condition.Operator.EQUAL, "SENSOR_001", null
        );

        PatternRepresentation.Condition condition2 = new PatternRepresentation.Condition(
                "temperature", PatternRepresentation.Condition.Operator.GREATER_THAN, 25, null
        );

        PatternRepresentation.Condition condition3 = new PatternRepresentation.Condition(
                "status", PatternRepresentation.Condition.Operator.NOT_EQUAL, "active", null
        );

        // Create events with conditions, quantifiers, and concatenators
        PatternRepresentation.Event event1 = new PatternRepresentation.Event(
                "start", List.of(condition1), PatternRepresentation.Quantifier.ParamFree.ONE_OR_MORE, null
        );

        PatternRepresentation.Event event2 = new PatternRepresentation.Event(
                "middle", List.of(condition2), new PatternRepresentation.Quantifier.NTimes(3), PatternRepresentation.Event.Concatenator.NEXT
        );

        PatternRepresentation.Event event3 = new PatternRepresentation.Event(
                "end", List.of(condition3), PatternRepresentation.Quantifier.ParamFree.OPTIONAL, PatternRepresentation.Event.Concatenator.NOT_NEXT
        );

        return new PatternRepresentation(List.of(event1, event2, event3), new PatternRepresentation.WithinClause(10), new PatternRepresentation.KeyByClause("sensor_id"));
    }

    private Pattern<BaseEvent, ?> createExpectedPattern() {
        // Manually construct the expected Flink Pattern that matches `PatternRepresentation`
        Pattern<BaseEvent, ?> expectedPattern = Pattern.<BaseEvent>begin("start")
                .where(new SimpleCondition<>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        return "SENSOR_001".equals(event.getAttribute("sensor_id"));
                    }
                })
                .oneOrMore()
                .next("middle")
                .where(new SimpleCondition<>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        return (int) event.getAttribute("temperature") > 25;
                    }
                })
                .times(3)
                .notNext("end") // Remove the optional quantifier from the negative pattern
                .where(new SimpleCondition<>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        return !"active".equals(event.getAttribute("status"));
                    }
                })
                .within(Duration.ofSeconds(10));

        return expectedPattern;
    }


    private void assertPatternsEqual(Pattern<BaseEvent, ?> expected, Pattern<BaseEvent, ?> actual) {
        // Compare by converting to string representation
        assertEquals(expected.toString(), actual.toString(), "The generated pattern does not match the expected pattern.");
    }

    @Test
    public void testEventCreation() {
        // Example of creating a GenericEvent
        GenericEvent event = new GenericEvent(123456789L); // Use timestamp only in constructor

        // Set attributes after instantiation
        event.setAttribute("sensor_id", "SENSOR_001");
        event.setAttribute("temperature", 25);
        event.setAttribute("status", "inactive");

        // Validate event attributes
        assertEquals("SENSOR_001", event.getAttribute("sensor_id"));
        assertEquals(25, event.getAttribute("temperature"));
        assertEquals("inactive", event.getAttribute("status"));
    }
}
