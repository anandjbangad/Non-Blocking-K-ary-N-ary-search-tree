import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.AtomicStampedReference;


public class KArySearchTree {
	public KAryInternalNode root;
	private int K;
	private static final AtomicReferenceFieldUpdater<KAryInternalNode, Info> infoUpdater =
	        AtomicReferenceFieldUpdater.newUpdater(KAryInternalNode.class, Info.class, "info");
	
	public KArySearchTree(int K){
		this(K, new KAryInternalNode(K, true));
	}
	
	public KArySearchTree(int K, KAryInternalNode root){
		this.root = root;
		this.K = K;
	}
	
	private KAryBaseNode searchKey(int key, KAryInternalNode l){
		if(l.keys[0] == Integer.MAX_VALUE)
			return l.children.get(0);
		int left = 0;
		int right = l.keyCount-1;
		while(right > left){
			int mid = (left+right)/2;
			if( key < l.keys[mid]){
				right = mid;
			}
			else{
				left = mid+1;
			}
		}
		if(left == l.keyCount-1 && (key >= l.keys[left]) )
			return l.children.get(l.keyCount);
		return l.children.get(left);
	}
	
	public boolean containsKey(int key){
		if(key == -1)
			return false;
		
		KAryBaseNode l = root.children.get(0);
		while( !l.isLeaf()){
			l = searchKey(key, (KAryInternalNode)l);
		}
		return ((KAryLeafNode)l).hasKey(key);
	}
	
	public boolean insert(int key){
		if(key == -1)
			return false;
		KAryInternalNode p;
		KAryBaseNode l,newChild;
		Info pinfo;
		int pindex;
		while(true){
			p = root;
			pinfo = p.info;
			l = p.children.get(0);
			
			while(!l.isLeaf()){
				p = (KAryInternalNode)l;
				l = searchKey(key,p);
			}
//			while(l.children != null){
//				p = (KAryInternalNode)l;
//				l =  searchKey(key,p);
//			}
			
			pinfo = p.info;
			KAryBaseNode currentL = (KAryBaseNode)p.children.get(p.keyCount);
			pindex = p.keyCount;
			for(int i = 0; i < p.keyCount; ++i){
				if( key < p.keys[i] ){
					currentL = (KAryBaseNode)p.children.get(i);
					pindex = i;
					break;
				}
			}
			
			if(l!= currentL)
				continue;
			if(((KAryLeafNode)l).hasKey(key))
				return false;
			else if(pinfo != null && pinfo.getClass() != Clean.class){
				help(pinfo);
			}
			else{
				if(l.keyCount == K-1){
					newChild = new KAryInternalNode(key, l);
				}
				else{
					newChild = new KAryLeafNode(key,(KAryLeafNode)l);
				}
				InsertInfo newPInfo = new InsertInfo(l,p,newChild,pindex);
				if( infoUpdater.compareAndSet(p, pinfo, newPInfo)){
					helpInsert(newPInfo);
					return true;
				}
				else{
					help(p.info);
				}
			}		
		}
	}
	
	public boolean remove(int key){
		if(key == -1)
			return false;
		KAryInternalNode gp,p;
		KAryBaseNode l,newChild;
		Info gpinfo,pinfo;
		int pindex;
		int gpindex;
		while(true){
			gp = null;
			p = root;
			pinfo = p.info;
			l = root.children.get(0);
			while(!l.isLeaf()){
				gp = p;
				p = (KAryInternalNode)l;
				l = searchKey(key, p);
			}

			gpinfo = gp.info;
			
			KAryBaseNode currentP = gp.children.get(gp.keyCount); 
			gpindex = gp.keyCount;
			for(int i = 0; i < gp.keyCount; ++i){
				if( key < gp.keys[i] ){
					currentP = gp.children.get(i);
					gpindex = i;
					break;
				}
			}
			if(p != currentP)
				continue;
			
			pinfo = p.info;
			KAryBaseNode currentL = p.children.get(p.keyCount);
			pindex = p.keyCount;
			for(int i = 0; i < p.keyCount; ++i){
				if( key < p.keys[i] ){
					currentL = p.children.get(i);
					pindex = i;
					break;
				}
			}
			
			if(l != currentL)
				continue;
			
			if(!((KAryLeafNode)l).hasKey(key))
				return false;
			else if(gpinfo!= null && gpinfo.getClass() != Clean.class)
				help(gpinfo);
			else if(pinfo!= null && pinfo.getClass() != Clean.class)
				help(pinfo);
			else{
				int ccount = 0;
				if(l.keyCount == 1){
					for(int i = 0; i < p.keyCount; ++i){
						if(p.children.get(i).keyCount > 0){
							ccount++;
						}
						if(ccount > 2)
							break;
					}
				}
				
				if(l.keyCount == 1 && ccount == 2){
					DeleteInfo newGPInfo = new DeleteInfo(l, p, gp, pinfo, gpindex);
					if(infoUpdater.compareAndSet(gp, gpinfo, newGPInfo)){
						if(helpDelete(newGPInfo))
							return true;
						else
							help(gp.info);
					}
				}
				//Simple Deletion
				else{
					newChild = new KAryLeafNode(key,(KAryLeafNode)l,true);
					InsertInfo newPInfo = new InsertInfo(l,p,newChild,pindex);
					if(infoUpdater.compareAndSet(p, pinfo, newPInfo)){
						helpInsert(newPInfo);
						return true;
					}
					else{
						help(p.info);
					}
				}
			}
			
		}
	}
	
	private void help(Info info){
		if(info.getClass() == InsertInfo.class){
			helpInsert((InsertInfo)info);
		}
		else if(info.getClass() == DeleteInfo.class){
			helpDelete((DeleteInfo)info);
		}
		else if(info.getClass() == MarkInfo.class){
			helpMarked(((MarkInfo)info).dinfo);
		}
	}
	
	private void helpInsert(InsertInfo info){
		info.p.children.compareAndSet(info.pindex, info.oldChild, info.newChild);
		infoUpdater.compareAndSet((KAryInternalNode)info.p, info, new Clean());
	}
	
	private boolean helpDelete(DeleteInfo info){
		boolean markSuccess = infoUpdater.compareAndSet((KAryInternalNode)info.p, info.pinfo, new MarkInfo(info));
		Info currentPInfo = ((KAryInternalNode)info.p).info;
		if(markSuccess || currentPInfo.getClass() == MarkInfo.class 
				&& ((MarkInfo)currentPInfo).dinfo == info ){
			helpMarked(info);
			return true;
		}
		else{
			help(currentPInfo);
			infoUpdater.compareAndSet((KAryInternalNode)info.gp, info, new Clean());
			return false;
		}
	}
	
	private void helpMarked(DeleteInfo info){
		KAryBaseNode other = info.p.children.get(info.p.keyCount-1);
		for(int i = 0; i < info.p.keyCount ;++i){
			KAryBaseNode u = info.p.children.get(i);
			if(u.keyCount > 0 && u != info.l ){
				other = u;
				break;
			}
		}
		info.gp.children.compareAndSet(info.gpindex, info.p, other);
		infoUpdater.compareAndSet((KAryInternalNode)info.gp, info, new Clean());
	}
}
