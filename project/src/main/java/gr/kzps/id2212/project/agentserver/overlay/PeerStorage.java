package gr.kzps.id2212.project.agentserver.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PeerStorage {
	private final Logger LOG = LogManager.getLogger(PeerStorage.class);

	private volatile List<PeerAgent> peerAgents;
	private Lock storeLock;
	private Integer sampleSize;

	public PeerStorage(PeerAgent local, Integer sampleSize) {
		peerAgents = new ArrayList<>();
		this.sampleSize = sampleSize;
		peerAgents.add(local);
		storeLock = new ReentrantLock();
	}

	public void addPeer(PeerAgent peer) {

		if (!peerAgents.contains(peer)) {
			storeLock.lock();
			peerAgents.add(peer);
			storeLock.unlock();
			LOG.debug("Added peer {} to storage", peer);
		} else {
			LOG.debug("Storage already contains {}", peer);
		}
	}

	public void mergeView(List<PeerAgent> view) {
		LOG.debug("Merging received view");

		storeLock.lock();
		List<PeerAgent> toMerge = view.stream()
				.distinct()
				.filter(v -> !peerAgents.contains(v))
				.collect(Collectors.toList());

		peerAgents.addAll(toMerge);
		storeLock.unlock();

		LOG.debug("Merged received view!");
	}

	public List<PeerAgent> createSample() throws PeerNotFound {
		LOG.debug("Creating sample");
		Integer actualSize = Math.min(sampleSize, peerAgents.size());
		List<PeerAgent> sample = new ArrayList<>();

		while (sample.size() < actualSize) {
			sample.add(getRandomPeer());
		}

		return sample;
	}

	public PeerAgent getRandomPeer() throws PeerNotFound {
		LOG.debug("Getting random peer BEFORE LOCK");

		if (!isEmpty()) {
			// Exclude local reference
			Integer rndPeer = ThreadLocalRandom.current().nextInt(1, peerAgents.size());
			LOG.debug("Getting random peer AFTER LOCk");
			return peerAgents.get(rndPeer);
		}

		throw new PeerNotFound("Local view is empty");
	}

	public PeerAgent getPeer(PeerAgent peer) throws PeerNotFound {

		Optional<PeerAgent> maybe = peerAgents.stream().filter(a -> a.equals(peer)).findFirst();

		if (maybe.isPresent()) {
			return maybe.get();
		} else {
			throw new PeerNotFound();
		}
	}

	public void removePeer(PeerAgent peer) {
		storeLock.lock();
		peerAgents.remove(peer);
		storeLock.unlock();
	}

	public PeerAgent getSelf() {
		return peerAgents.get(0);
	}

	private Boolean isEmpty() {
		// Do not count the self reference
		return peerAgents.size() < 2;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		peerAgents.stream().forEach(p -> sb.append(p).append("\n"));

		return sb.toString();
	}
}
