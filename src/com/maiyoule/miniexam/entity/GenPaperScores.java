package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class GenPaperScores implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8199085346688588739L;
		private int total;
		private int pass;
		
		private int radio;
		private int multi;
		private int judge;
		public int getTotal() {
			return total;
		}
		public void setTotal(int total) {
			this.total = total;
		}
		public int getPass() {
			return pass;
		}
		public void setPass(int pass) {
			this.pass = pass;
		}
		public int getRadio() {
			return radio;
		}
		public void setRadio(int radio) {
			this.radio = radio;
		}
		public int getMulti() {
			return multi;
		}
		public void setMulti(int multi) {
			this.multi = multi;
		}
		public int getJudge() {
			return judge;
		}
		public void setJudge(int judge) {
			this.judge = judge;
		}
		
		
}
