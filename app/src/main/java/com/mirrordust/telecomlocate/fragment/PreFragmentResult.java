package com.mirrordust.telecomlocate.fragment;

public class PreFragmentResult {
    public String title;
    public String prediction;
    public String actually;
    public String error;
    public String time;

    public PreFragmentResult(){
        this.title="";
        this.prediction="";
        this.actually="";
        this.error="";
        this.time="";
    }
    public PreFragmentResult(String t,String p,String a,String e,String tm){
        this.title=t;
        this.prediction=p;
        this.actually=a;
        this.error=e;
        this.time=tm;
    }
}
