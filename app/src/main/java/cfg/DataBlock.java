package cfg;

import java.util.ArrayList;

import cfg.expr.data.CFGArray;

public record DataBlock(ArrayList<CFGArray> data){
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("data:\n");
        for(CFGArray arr : data) {
            sb.append("global array "+arr.name()+": { ");
            for(int i = 0; i < arr.size(); i++) {
                sb.append(arr.elems()[i].toString());
                if(i != arr.size() - 1) {
                    sb.append(",");
                }
                sb.append(" ");
            }
            sb.append("}\n");

        }


        return sb.toString();
    }

}
