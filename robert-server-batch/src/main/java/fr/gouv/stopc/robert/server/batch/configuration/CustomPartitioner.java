package fr.gouv.stopc.robert.server.batch.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

public class CustomPartitioner implements Partitioner {

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {

		Map<String, ExecutionContext> result = new HashMap<>(gridSize);

		final int range = ContactsProcessingConfiguration.CHUNK_SIZE;
		int start = 0;
		int end = range - 1;

		for (int i = 0; i < gridSize; i++) {
			ExecutionContext value = new ExecutionContext();

			value.putInt("start", start);
			value.putInt("end", end);
			value.putString("name", "Thread " + i);
			result.put("Partition " + i, value);

			start += range;
			end += range;
		}

		return result;
	}

}
