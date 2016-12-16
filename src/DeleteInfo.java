
public class DeleteInfo implements Info {
	KAryBaseNode p,gp;
	KAryBaseNode l;
	Info pinfo;
	int gpindex;
	DeleteInfo(KAryBaseNode l,KAryBaseNode p, KAryBaseNode gp,
			Info pinfo, int gpindex){
		this.p = p;
		this.l = l;
		this.gp = gp;
		this.pinfo = pinfo;
		this.gpindex = gpindex;
	}
	
	@Override
	public boolean equals(Object o){
		DeleteInfo x = (DeleteInfo)o;
		if(x.p != p
			|| x.gp != gp
			|| x.l != l
			|| x.pinfo != pinfo
			|| x.gpindex != gpindex)
			return false;
		return true;
	}
}