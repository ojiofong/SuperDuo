package it.jaschke.alexandria;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

/**
 * Created by oofong25 on 9/1/15.
 */
public class DetailActivity extends AppCompatActivity implements BookDetail.BookDetailCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        initToolBar();
        if (savedInstanceState == null) {
            initBookDetailFragment();
        }
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.book_detail);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.book_detail);
    }

    private void initBookDetailFragment() {
        String ean = getIntent().getStringExtra(BookDetail.EAN_KEY);
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);
        BookDetail bookDetail = new BookDetail();
        bookDetail.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.container1, bookDetail).commit();

    }

    public void goBack(View view) {
        onBackPressed();
    }

    @Override
    public void onDeleteBook() {
        Toast.makeText(getApplicationContext(), getString(R.string.book_deleted), Toast.LENGTH_LONG).show();
        finish();
    }
}
