package com.escalon.JustChat;

import android.app.Application;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.app.ProgressDialog;
import android.util.LruCache;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
//import com.google.mlkit.samples.nl.translate.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TranslateViewModel extends AndroidViewModel {
    // This specifies the number of translators instance we want to keep in our LRU cache.
    // Each instance of the translator is built with different options based on the source
    // language and the target language, and since we want to be able to manage the number of
    // translator instances to keep around, an LRU cache is an easy way to achieve this.
    private static final int NUM_TRANSLATORS = 3;

    private final RemoteModelManager modelManager;
    private final LruCache<TranslatorOptions, Translator> translators =
            new LruCache<TranslatorOptions, Translator>(NUM_TRANSLATORS) {
                @Override
                public Translator create(TranslatorOptions options) {
                    return Translation.getClient(options);
                }

                @Override
                public void entryRemoved(
                        boolean evicted, TranslatorOptions key, Translator oldValue, Translator newValue) {
                    oldValue.close();
                }
            };
    public MutableLiveData<Language> sourceLang = new MutableLiveData<>();
    public MutableLiveData<Language> targetLang = new MutableLiveData<>();
    public MutableLiveData<String> sourceText = new MutableLiveData<>();
    public MediatorLiveData<ResultOrError> translatedText = new MediatorLiveData<>();
    public MutableLiveData<List<String>> availableModels = new MutableLiveData<>();

    public TranslateViewModel(@NonNull Application application) {
        super(application);
        modelManager = RemoteModelManager.getInstance();

        // Create a translation result or error object.
        final OnCompleteListener<String> processTranslation =
                new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            translatedText.setValue(new ResultOrError(task.getResult(), null));
                        } else {
                            translatedText.setValue(new ResultOrError(null, task.getException()));
                        }
                        // Update the list of downloaded models as more may have been
                        // automatically downloaded due to requested translation.
                        fetchDownloadedModels();
                    }
                };

        // Start translation if any of the following change: input text, source lang, target lang.
        translatedText.addSource(
                sourceText,
                new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        translate().addOnCompleteListener(processTranslation);
                    }
                });
        Observer<Language> languageObserver =
                new Observer<Language>() {
                    @Override
                    public void onChanged(@Nullable Language language) {
                        translate().addOnCompleteListener(processTranslation);
                    }
                };
        translatedText.addSource(sourceLang, languageObserver);
        translatedText.addSource(targetLang, languageObserver);

        // Update the list of downloaded models.
        fetchDownloadedModels();
    }

    // Gets a list of all available translation languages.
    public List<Language> getAvailableLanguages() {
//        if want to add all languages
//        List<Language> languages = new ArrayList<>();
//        List<String> languageIds = TranslateLanguage.getAllLanguages();
//        for (String languageId : languageIds) {
//            languages.add(new Language(TranslateLanguage.fromLanguageTag(languageId)));
//        }
//       [en - English, fr - French, hi - Hindi, ja - Japanese, es - Spanish]
//        System.out.println("languagesAL::::::::::::::::"+languageIds);
        List<Language> paper_language = new ArrayList<>();
        paper_language.add(new Language(TranslateLanguage.fromLanguageTag("en")));
        paper_language.add(new Language(TranslateLanguage.fromLanguageTag("fr")));
        paper_language.add(new Language(TranslateLanguage.fromLanguageTag("hi")));
        paper_language.add(new Language(TranslateLanguage.fromLanguageTag("ja")));
        paper_language.add(new Language(TranslateLanguage.fromLanguageTag("es")));
        return paper_language;
    }

    private TranslateRemoteModel getModel(String languageCode) {
        return new TranslateRemoteModel.Builder(languageCode).build();
    }

    // Updates the list of downloaded models available for local translation.
    private void fetchDownloadedModels() {
        modelManager
                .getDownloadedModels(TranslateRemoteModel.class)
                .addOnSuccessListener(
                        new OnSuccessListener<Set<TranslateRemoteModel>>() {
                            @Override
                            public void onSuccess(Set<TranslateRemoteModel> remoteModels) {
                                List<String> modelCodes = new ArrayList<>(remoteModels.size());
                                for (TranslateRemoteModel model : remoteModels) {
                                    modelCodes.add(model.getLanguage());
                                }
                                Collections.sort(modelCodes);
                                availableModels.setValue(modelCodes);
                            }
                        });
    }

    // Starts downloading a remote model for local translation.
    public void downloadLanguage(Language language, FragmentActivity activity) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Downloading language: "+language.getDisplayName()+"\n\nThis may take some time please wait");
        progressDialog.show();
        TranslateRemoteModel model = getModel(TranslateLanguage.fromLanguageTag(language.getCode()));
        modelManager
                .download(model, new DownloadConditions.Builder().build())
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                fetchDownloadedModels();
                            }
                        });
    }



    // Deletes a locally stored translation model.
    public void deleteLanguage(Language language, FragmentActivity activity) {
        TranslateRemoteModel model = getModel(TranslateLanguage.fromLanguageTag(language.getCode()));
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Deleting language: "+language.getDisplayName());
        progressDialog.show();
        modelManager
                .deleteDownloadedModel(model)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                fetchDownloadedModels();
                            }
                        });
    }

    public Task<String> translate() {
        final String text = sourceText.getValue();
        final Language source = sourceLang.getValue();
        final Language target = targetLang.getValue();
        if (source == null || target == null || text == null || text.isEmpty()) {
            return Tasks.forResult("");
        }
        String sourceLangCode = TranslateLanguage.fromLanguageTag(source.getCode());
        String targetLangCode = TranslateLanguage.fromLanguageTag(target.getCode());
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLangCode)
                        .setTargetLanguage(targetLangCode)
                        .build();
        return translators
                .get(options)
                .downloadModelIfNeeded()
                .continueWithTask(
                        new Continuation<Void, Task<String>>() {
                            @Override
                            public Task<String> then(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    return translators.get(options).translate(text);
                                } else {
                                    Exception e = task.getException();
                                    if (e == null) {
                                        e = new Exception(getApplication().getString(R.string.common_google_play_services_unknown_issue));
                                    }
                                    return Tasks.forException(e);
                                }
                            }
                        });
    }

    /** Holds the result of the translation or any error. */
    public static class ResultOrError {
        public final String result;
        public final Exception error;

        ResultOrError(@Nullable String result, @Nullable Exception error) {
            this.result = result;
            this.error = error;
        }
    }

    /**
     * Holds the language code (i.e. "en") and the corresponding localized full language name (i.e.
     * "English")
     */
    public static class Language implements Comparable<Language> {
        private final String code;

        public Language(String code) {
            this.code = code;
        }

        public String getDisplayName() {
            return new Locale(code).getDisplayName();
        }

        public String getCode() {
            return code;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Language)) {
                return false;
            }

            Language otherLang = (Language) o;
            return otherLang.code.equals(code);
        }

        @NonNull
        @Override
        public String toString() {
            return code + " - " + getDisplayName();
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }

        @Override
        public int compareTo(@NonNull Language o) {
            return this.getDisplayName().compareTo(o.getDisplayName());
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Each new instance of a translator needs to be closed appropriately. Here we utilize the
        // ViewModel's onCleared() to clear our LruCache and close each Translator instance when
        // this ViewModel is no longer used and destroyed.
        translators.evictAll();
    }
}