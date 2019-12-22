package itp341.caceres.nicholas.positive_note.app;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import itp341.caceres.nicholas.positive_note.app.modelClasses.EdMaterialSingleton;
import itp341.caceres.nicholas.positive_note.app.modelClasses.EducationMaterial;

/**
 * Created by NLCaceres on 4/30/2016.
 */
public class FragmentEducation extends Fragment {

  private ListView educationList;
  private educationAdapter edAdapter;

  private EdMaterialSingleton edMats;

  public static FragmentEducation newInstance(String name) {
    FragmentEducation edFrag = new FragmentEducation();
    return edFrag;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_education, container, false);

    edMats = EdMaterialSingleton.getInstance();

    educationList = (ListView) v.findViewById(R.id.educationListView);
    edAdapter = new educationAdapter(getContext(), edMats.getEdMats());
    educationList.setAdapter(edAdapter);

    return v;
  }

  public class educationAdapter extends ArrayAdapter<EducationMaterial> {
    public educationAdapter(Context context, ArrayList<EducationMaterial> educationMaterial) {
      super(context, 0, educationMaterial);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_education_material, parent, false);
      }

      final EducationMaterial education = getItem(position);

      TextView sectionTitle = (TextView) convertView.findViewById(R.id.sectionTitleTV);
      sectionTitle.setText(education.getSectionTitle());

      TextView firstURL = (TextView) convertView.findViewById(R.id.firstEdURL);
      TextView secondURL = (TextView) convertView.findViewById(R.id.secondEdURL);
      TextView thirdURL = (TextView) convertView.findViewById(R.id.thirdEdURL);

      if (education.getSectionTitle().equals("On Mental Health: It's Normal")) {
        firstURL.setTextColor(Color.parseColor("#000000"));
        firstURL.setText(education.getSectionFirstURLTitle());
        secondURL.setTextColor(Color.parseColor("#000000"));
        secondURL.setText(education.getSectionSecondURLTitle());
        thirdURL.setTextColor(Color.parseColor("#000000"));
        thirdURL.setText(education.getSectionThirdURLTitle());
        return convertView;
      }

      View.OnClickListener textViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          TextView textViewClicked = (TextView) view;
          String textViewText = textViewClicked.getText().toString();

//                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//                    builder.setToolbarColor(getResources().getColor(R.color.appColorPrimary));
//                    CustomTabsIntent customTabsIntent = builder.build();
          // ChromeTabs above vs WebView Frag below
          FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
          fragTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
          fragTransaction.addToBackStack(null);

          if (textViewClicked.getId() == R.id.firstEdURL && textViewText.equals(education.getSectionFirstURLTitle())) {
            Toast.makeText(getContext(), "First URL Clicked: " + education.getSectionFirstURL(), Toast.LENGTH_SHORT).show();
            //customTabsIntent.launchUrl(getContext(), Uri.parse(education.getSectionFirstURL()));
            FragmentTherapy therapyFrag = FragmentTherapy.newInstance(education.getSectionFirstURL());
            fragTransaction.replace(R.id.root_frame, therapyFrag).commit();
          } else if (textViewClicked.getId() == R.id.secondEdURL && textViewText.equals(education.getSectionSecondURLTitle())) {
            Toast.makeText(getContext(), "Second URL Clicked: " + education.getSectionSecondURL(), Toast.LENGTH_SHORT).show();
            //customTabsIntent.launchUrl(getContext(), Uri.parse(education.getSectionSecondURL()));
            FragmentTherapy therapyFrag = FragmentTherapy.newInstance(education.getSectionSecondURL());
            fragTransaction.replace(R.id.root_frame, therapyFrag).commit();
          } else if (textViewClicked.getId() == R.id.thirdEdURL && textViewText.equals(education.getSectionThirdURLTitle())) {
            Toast.makeText(getContext(), "Third URL Clicked: " + education.getSectionThirdURL(), Toast.LENGTH_SHORT).show();
            //customTabsIntent.launchUrl(getContext(), Uri.parse(education.getSectionThirdURL()));
            FragmentTherapy therapyFrag = FragmentTherapy.newInstance(education.getSectionThirdURL());
            fragTransaction.replace(R.id.root_frame, therapyFrag).commit();
          }
        }
      };

      firstURL.setOnClickListener(textViewListener);
      SpannableString firstLinkUnderlined = new SpannableString(education.getSectionFirstURLTitle());
      firstLinkUnderlined.setSpan(new UnderlineSpan(), 0, education.getSectionFirstURLTitle().length(), 0);
      firstURL.setText(firstLinkUnderlined);
      //firstURL.setText(education.getSectionFirstURLTitle());

      secondURL.setOnClickListener(textViewListener);
      SpannableString secondLinkUnderlined = new SpannableString(education.getSectionSecondURLTitle());
      secondLinkUnderlined.setSpan(new UnderlineSpan(), 0, education.getSectionSecondURLTitle().length(), 0);
      secondURL.setText(secondLinkUnderlined);

      thirdURL.setOnClickListener(textViewListener);
      SpannableString thirdLinkUnderlined = new SpannableString(education.getSectionThirdURLTitle());
      thirdLinkUnderlined.setSpan(new UnderlineSpan(), 0, education.getSectionThirdURLTitle().length(), 0);
      thirdURL.setText(thirdLinkUnderlined);
      return convertView;
    }
  }

//    private class LinkSpan extends URLSpan {
//        private LinkSpan(String url) {
//            super(url);
//        }
//
//        @Override
//        public void onClick(View view) {
//            String url = getURL();
//            Log.d("Link Span Clicker", "This got clicked with this link: " + url);
//        }
//    }
}


