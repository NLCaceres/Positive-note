package itp341.caceres.nicholas.positive_note.app.Model;

import java.util.ArrayList;

/**
 * Created by NLCaceres on 5/3/2016.
 */
public class EdMaterialSingleton {
    private static EdMaterialSingleton ourInstance = new EdMaterialSingleton();

    public static EdMaterialSingleton getInstance() {
        return ourInstance;
    }

    private ArrayList<EducationMaterial> edMats;

    private EdMaterialSingleton() {
        edMats = new ArrayList<>();
        edMats.add(new EducationMaterial("On Mental Health: It's Normal", "How long have you felt this way?",
                "What might bothers you the most?", "How much does it bother you?"));

        edMats.add(new EducationMaterial("Mood Concerns", "Depression Overview", "https://tinyurl.com/qe8q78h",
                "Bipolar Overview", "https://tinyurl.com/h6h4opx", "Schizophrenia Overview", "https://bit.ly/2dZkJ4N"));

        edMats.add(new EducationMaterial("Anxiety Concerns", "Phobias", "https://tinyurl.com/oyfhzcz",
                "Generalized Anxiety", "https://bit.ly/2HnspMO", "PTSD: Post Traumatic Stress Disorder", "https://bit.ly/2rF3Ofi"));
        edMats.add(new EducationMaterial("Eating Concerns", "Bulimia", "https://bit.ly/2HmilSy", "Anorexia",
                "https://bit.ly/2FLEwBy", "Problematic Hollywood", "https://bit.ly/2U0l8b4"));
        edMats.add(new EducationMaterial("Personality Concerns", "Personality Disorders Overview", "https://tinyurl.com/j9ylhnr",
                "Getting A Helping Hand", "https://tinyurl.com/jy4aq52", "Ten Types", "https://bit.ly/2JPUD2F"));

    }

    public ArrayList<EducationMaterial> getEdMats() {
        return edMats;
    }
}
