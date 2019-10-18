package itp341.caceres.nicholas.positive_note.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class RootFragment extends Fragment { // RootFragment exists to hold either TherapyFrag (for it's webView) or a EducationFrag (for listview of links)

  public static RootFragment newInstance() {
    RootFragment rootFrag = new RootFragment();
    return rootFrag;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.root_fragment, container, false);

    FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.replace(R.id.root_frame, EducationFragment.newInstance(null)).commit(); // Fill on creation and commit

    return view;
  }
}
