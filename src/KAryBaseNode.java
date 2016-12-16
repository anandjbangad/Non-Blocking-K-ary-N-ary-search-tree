import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicStampedReference;


public class KAryBaseNode {
	
	public int keyCount;
	public int[] keys;
	public AtomicReferenceArray<KAryBaseNode> children;
	public AtomicStampedReference<KAryBaseNode> next;
	public KAryBaseNode(){
		this.keys = null;
		this.keyCount = 0;
		this.next = new AtomicStampedReference<KAryBaseNode>(null, 0);
	}
	
	protected boolean isLeaf(){
		return false;
	}
}
