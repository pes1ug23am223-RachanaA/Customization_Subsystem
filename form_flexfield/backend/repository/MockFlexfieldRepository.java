package backend.repository;

import backend.dto.FlexFieldDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CLASS: MockFlexfieldRepository
 * Subsystem: Customization — Flexfield Manager
 *
 * PURPOSE:
 *   In-memory mock implementation of IFlexfieldRepository.
 *   Used for testing when DB_INTEGRATION = false.
 *   Stores all flexfields in memory; data is lost on restart.
 */
public class MockFlexfieldRepository implements IFlexfieldRepository {

    private final Map<Integer, FlexFieldDTO> flexfields = new HashMap<>();
    private int flexfieldIdCounter = 1;

    @Override
    public void addField(String name, String type, String segments) {
        int fieldId = flexfieldIdCounter++;
        FlexFieldDTO field = new FlexFieldDTO(fieldId, name, type, segments);
        flexfields.put(fieldId, field);
        System.out.println("[MockFlexfieldRepository] Added flexfield: " + name + " (id=" + fieldId + ")");
    }

    @Override
    public FlexFieldDTO getFieldById(int fieldId) {
        FlexFieldDTO field = flexfields.get(fieldId);
        if (field == null) {
            System.out.println("[MockFlexfieldRepository] Flexfield " + fieldId + " not found");
        }
        return field;
    }

    @Override
    public List<FlexFieldDTO> getAllFlexfields() {
        return new ArrayList<>(flexfields.values());
    }

    @Override
    public void removeField(int fieldId) {
        flexfields.remove(fieldId);
        System.out.println("[MockFlexfieldRepository] Deleted flexfield: " + fieldId);
    }

    @Override
    public void updateFieldSegment(int fieldId, int segmentIndex, String value) {
        FlexFieldDTO field = flexfields.get(fieldId);
        if (field != null) {
            String segments = field.getSegments();
            String[] segmentArray = segments != null ? segments.split(",") : new String[0];
            if (segmentIndex >= 0 && segmentIndex < segmentArray.length) {
                segmentArray[segmentIndex] = value;
                String updatedSegments = String.join(",", segmentArray);
                flexfields.put(fieldId, new FlexFieldDTO(fieldId, field.getFieldName(), field.getFieldType(), updatedSegments));
                System.out.println("[MockFlexfieldRepository] Updated segment " + segmentIndex + " for flexfield " + fieldId);
            }
        }
    }

    @Override
    public boolean validateField(int fieldId) {
        return flexfields.containsKey(fieldId);
    }

    @Override
    public List<String> getValues(int fieldId) {
        List<String> values = new ArrayList<>();
        FlexFieldDTO field = flexfields.get(fieldId);
        if (field != null && field.getSegments() != null) {
            String[] segmentArray = field.getSegments().split(",");
            for (String segment : segmentArray) {
                values.add(segment.trim());
            }
        }
        return values;
    }
}
