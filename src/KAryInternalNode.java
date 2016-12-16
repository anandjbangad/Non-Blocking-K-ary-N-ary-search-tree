import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicStampedReference;


public class KAryInternalNode extends KAryBaseNode {
	
	//public AtomicReferenceArray<AtomicStampedReference<KAryInternalNode>> children;
	
	public volatile Info info = null;
	
	public KAryInternalNode(int K, boolean root){
		this.keys = new int[K-1];
		this.keyCount = K-1;
		for(int i = 0; i < keyCount; ++i){
			this.keys[i] = Integer.MAX_VALUE;
		}
		if(root){
			this.children = new AtomicReferenceArray<KAryBaseNode>(K);
			this.children.set(0, new KAryInternalNode(K,false));
			
			//this.children.set(0, new KAryTreeNode<E, V>(null,null));
			for(int i = 1; i < K; ++i ){
				this.children.set(i, new KAryLeafNode(K));
			}
		}
		else{
			this.children = new AtomicReferenceArray<KAryBaseNode>(K);
			for(int i = 0; i < K; ++i ){
				this.children.set(i, new KAryLeafNode(K));
			}
		}
	}
	
	
	public KAryInternalNode(int key, KAryBaseNode leaf){
		this.keyCount = leaf.keyCount;
		int i;
		for(i = 0; i < keyCount; ++i){
			if( key < leaf.keys[i])
				break;
		}
		this.children = new AtomicReferenceArray<KAryBaseNode>(keyCount+1);
		for(int j = 0; j < i; ++j){
			this.children.set(j, new KAryLeafNode(leaf.keys[j],true));
		}
		this.children.set(i, new KAryLeafNode(key,true));
		for(int j = i ; j < keyCount; ++j){
			this.children.set(j+1, new KAryLeafNode(leaf.keys[j],true));
		}
		this.keys = new int[keyCount];
		for(int j = 0; j < keyCount; ++j){
			this.keys[j] = this.children.get(j+1).keys[0];
		}
	}
	
	protected boolean isLeaf(){
		return false;
	}
	
}
