package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.Item;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.MyDivideItemDecoration;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.SingleEditTextDialog;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class ItemSetupFragment extends Fragment {
    private MyAdapter<Item> adapter;
    private List<Item> itemList;
    private RecyclerView recyclerView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemList = new ArrayList<>();
        itemList.addAll(DbManager.findAll(Item.class));
        itemList.add(new Item());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adapter = new MyAdapter<Item>(itemList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, Item data) {
                if(myViewHolder.getAdapterPosition() == itemList.size() - 1){
                    myViewHolder.getView(R.id.button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            add(myViewHolder.getAdapterPosition());
                        }
                    });
                }else{
                    final SwipeMenuLayout menuLayout = myViewHolder.getView(R.id.swipemenulayout);
                    myViewHolder.setText(R.id.nameTextView,data.getName());
                    myViewHolder.getView(R.id.root).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            menuLayout.smoothExpand();
                        }
                    });
                    myViewHolder.getView(R.id.dele).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            menuLayout.smoothClose();
                            dele(myViewHolder.getAdapterPosition());
                        }
                    });
                    myViewHolder.getView(R.id.edit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            menuLayout.smoothClose();
                            edit(myViewHolder.getAdapterPosition());
                        }
                    });

                }
            }

            @Override
            public int getLayoutId(int position) {
                if(position == itemList.size() - 1)
                    return R.layout.item_additem;
                return R.layout.item_item;
            }
        };
        View view = inflater.inflate(R.layout.single_recyclerview_layout,container,false);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void add(final int position) {
        final SingleEditTextDialog dialog = new SingleEditTextDialog(getContext());
        dialog.setDialogCallback(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                String s = (String) values[0];
                if (TextUtils.isEmpty(s)) {
                    dialog.showError("请输入名称！");
                    return;
                }
                if (DbManager.isExist(Item.class,"name = ?",s)) {
                    dialog.showError("已存在该项目，请改名！");
                    return;
                }

                Item item = new Item();
                item.setName(s);
                item.save();
                itemList.add(position, DbManager.findLast(Item.class));

                dialog.dismiss();
                adapter.notifyItemRangeChanged(position, 2);
                recyclerView.smoothScrollToPosition(position + 1);
            }
        });
        dialog.showAtLocation(recyclerView, Gravity.BOTTOM, 0, 0);
        dialog.setTitle("新增项目");
        dialog.setHint("请输入名称");
        dialog.setIco(getResources().getDrawable(R.drawable.additem,null));
    }

    private void edit(final int position) {
        final Item item = itemList.get(position);
        final SingleEditTextDialog dialog = new SingleEditTextDialog(getContext());
        dialog.setDialogCallback(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                String s = (String) values[0];
                String oldName = item.getName();
                if (TextUtils.isEmpty(s)) {
                    dialog.showError("请输入名称！");
                    return;
                }
                if (!oldName.equals(s)) {
                    if (DbManager.isExist(Item.class,"name = ?",s)){
                        dialog.showError("已存在该项目，请改名！");
                        return;
                    }
                    item.setName(s);
                    item.update();
                    adapter.notifyItemChanged(position);
                }
                dialog.dismiss();
            }
        });
        dialog.showAtLocation(recyclerView, Gravity.BOTTOM, 0, 0);
        dialog.setTitle("修改信息");
        dialog.setIco(getResources().getDrawable(R.drawable.edit,null));
        dialog.setEditText(item.getName());
    }

    private void dele(final int position){
        final Item item = itemList.get(position);
        DialogFactory dialogFactory = DialogFactory.getConfirmDialog("请确认",
                "是否删除 【"+item.getName()+"】 ？",
                R.drawable.warnning);
        dialogFactory.setDialogCallback(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                if(confirmFlag){
                    item.dele();
                    itemList.remove(position);
                    adapter.notifyItemRemoved(position);
                }
            }
        });
        dialogFactory.show(getFragmentManager(),"dele");
    }
}
