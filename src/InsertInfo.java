
public class InsertInfo implements Info{
	KAryBaseNode p;
	KAryBaseNode  oldChild,newChild;
	int pindex;
	InsertInfo( KAryBaseNode oldChild, KAryBaseNode p, KAryBaseNode newChild,
			int pindex){
		this.p = p;
		this.oldChild = oldChild;
		this.newChild = newChild;
		this.pindex = pindex;
	}
	
	//@Override
	public boolean equals(Object o){
		InsertInfo x = (InsertInfo)o;
		if(x.p != p
			|| x.oldChild != oldChild
			|| x.newChild != newChild
			|| x.pindex != pindex)
			return false;
		return true;
	}
}
