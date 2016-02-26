package com.maiyoule.miniexam.entity;

public class SyncUserStatus implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2314067741781462040L;
	private short status=0;
	private int total=0;
	private int readed=0;
	private int flished=0;
	private short opstatus=0;

	public static final short STATUS_EMPTY=0;
	public static final short STATUS_WAITING=1;
	public static final short STATUS_DOING=2;
	public static final short STATUS_FLISH=3;
	public static final short STATUS_CANCEL=-2;
	public static final short STATUS_NO_USER=-1;
	public static final short STATUS_EXCEPTION=-3;
	
	public static final short OP_CANCEL=1;
	public static final short OP_OK=0;
	
	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		synchronized (this) {
			this.status = status;
		}
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		synchronized (this) {
			this.total = total;
		}
	}

	public int getReaded() {
		return readed;
	}

	public void setReaded(int readed) {
		synchronized (this) {
			this.readed = readed;
		}
	}

	public int getFlished() {
		return flished;
	}

	public void setFlished(int flished) {
		synchronized (this) {
			this.flished = flished;
		}
	}

	public short getOpstatus() {
		return opstatus;
	}

	public void setOpstatus(short opstatus) {
		synchronized (this) {
			this.opstatus = opstatus;
		}
	}

	private static SyncUserStatus _status;

	public static SyncUserStatus getInstance() {
		if (_status == null) {
			_status = new SyncUserStatus();
		}
		return _status;
	}
}
