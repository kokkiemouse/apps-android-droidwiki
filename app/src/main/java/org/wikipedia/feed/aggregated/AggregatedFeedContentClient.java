package org.wikipedia.feed.aggregated;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.wikipedia.dataclient.WikiSite;
import org.wikipedia.dataclient.retrofit.RetrofitFactory;
import org.wikipedia.feed.FeedContentType;
import org.wikipedia.feed.FeedCoordinator;
import org.wikipedia.feed.dataclient.FeedClient;
import org.wikipedia.feed.image.FeaturedImageCard;
import org.wikipedia.feed.model.Card;
import org.wikipedia.feed.model.UtcDate;
import org.wikipedia.settings.Prefs;
import org.wikipedia.util.DateUtil;
import org.wikipedia.util.log.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

import static org.wikipedia.Constants.ACCEPT_HEADER_PREFIX;

public class AggregatedFeedContentClient {
    @NonNull private Map<String, Call<AggregatedFeedContent>> calls = new HashMap<>();
    @NonNull private Map<String, AggregatedFeedContent> aggregatedResponses = new HashMap<>();
    private int numResponsesExpected;
    private int numResponsesReceived;
    private int aggregatedResponseAge = -1;

    public static class OnThisDayFeed extends BaseClient {
        public OnThisDayFeed(@NonNull AggregatedFeedContentClient aggregatedClient) {
            super(aggregatedClient);
        }

        @Override
        void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                 @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards) {
//            for (String appLangCode : WikipediaApp.getInstance().language().getAppLanguageCodes()) {
//                if (responses.containsKey(appLangCode)
//                        && !FeedContentType.ON_THIS_DAY.getLangCodesDisabled().contains(appLangCode)) {
//                    AggregatedFeedContent content = responses.get(appLangCode);
//                    if (content.onthisday() != null && !content.onthisday().isEmpty()) {
//                        outCards.add(new OnThisDayCard(content.onthisday(), WikiSite.forLanguageCode(appLangCode), age));
//                    }
//                }
//            }
        }
    }
    public static class InTheNews extends BaseClient {
        public InTheNews(@NonNull AggregatedFeedContentClient aggregatedClient) {
            super(aggregatedClient);
        }

        @Override
        void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                 @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards) {
            // todo: remove age check when news endpoint provides dated content, T139481.
//            for (String appLangCode : WikipediaApp.getInstance().language().getAppLanguageCodes()) {
//                if (responses.containsKey(appLangCode)
//                        && !FeedContentType.NEWS.getLangCodesDisabled().contains(appLangCode)) {
//                    AggregatedFeedContent content = responses.get(appLangCode);
//                    if (age == 0 && content.news() != null) {
//                        outCards.add(new NewsListCard(content.news(), age, WikiSite.forLanguageCode(appLangCode)));
//                    }
//                }
//            }
        }
    }

    public static class FeaturedArticle extends BaseClient {
        public FeaturedArticle(@NonNull AggregatedFeedContentClient aggregatedClient) {
            super(aggregatedClient);
        }

        @Override
        void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                 @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards) {
//            for (String appLangCode : WikipediaApp.getInstance().language().getAppLanguageCodes()) {
//                if (responses.containsKey(appLangCode)
//                        && !FeedContentType.FEATURED_ARTICLE.getLangCodesDisabled().contains(appLangCode)) {
//                    AggregatedFeedContent content = responses.get(appLangCode);
//                    if (content.tfa() != null) {
//                        outCards.add(new FeaturedArticleCard(content.tfa(), age, WikiSite.forLanguageCode(appLangCode)));
//                    }
//                }
//            }
        }
    }

    public static class TrendingArticles extends BaseClient {
        public TrendingArticles(@NonNull AggregatedFeedContentClient aggregatedClient) {
            super(aggregatedClient);
        }

        @Override
        void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                 @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards) {
//            for (String appLangCode : WikipediaApp.getInstance().language().getAppLanguageCodes()) {
//                if (responses.containsKey(appLangCode)
//                        && !FeedContentType.TRENDING_ARTICLES.getLangCodesDisabled().contains(appLangCode)) {
//                    AggregatedFeedContent content = responses.get(appLangCode);
//                    if (content.mostRead() != null) {
//                        outCards.add(new MostReadListCard(content.mostRead(), WikiSite.forLanguageCode(appLangCode)));
//                    }
//                }
//            }
        }
    }

    public static class FeaturedImage extends BaseClient {
        public FeaturedImage(@NonNull AggregatedFeedContentClient aggregatedClient) {
            super(aggregatedClient);
        }

        @Override
        void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                 @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards) {
            if (responses.containsKey(wiki.languageCode())) {
                AggregatedFeedContent content = responses.get(wiki.languageCode());
                if (content.potd() != null) {
                    outCards.add(new FeaturedImageCard(content.potd(), age, wiki));
                }
            }
        }
    }

    void addAggregatedResponse(@Nullable AggregatedFeedContent content, int age, @NonNull WikiSite wiki) {
        aggregatedResponses.put(wiki.languageCode(), content);
        this.aggregatedResponseAge = age;
    }

    void requestAggregated(int age, @NonNull retrofit2.Callback<AggregatedFeedContent> cb) {
        cancel();
        UtcDate date = DateUtil.getUtcRequestDateFor(age);
        numResponsesExpected = FeedContentType.getAggregatedLanguages().size();
        for (String lang : FeedContentType.getAggregatedLanguages()) {
            WikiSite wiki = WikiSite.forLanguageCode(lang);
            String endpoint = String.format(Locale.ROOT, Prefs.getRestbaseUriFormat(), wiki.scheme(), wiki.authority());
            Retrofit retrofit = RetrofitFactory.newInstance(endpoint, wiki);
            AggregatedFeedContentClient.Service service = retrofit.create(Service.class);
            Call<AggregatedFeedContent> call = service.get(lang, date.year(), date.month(), date.date());
            call.enqueue(cb);
            calls.put(lang, call);
        }
    }

    public void cancel() {
        for (Call call : calls.values()) {
            call.cancel();
        }
        calls.clear();
        numResponsesReceived = 0;
        numResponsesExpected = 0;
    }

    private interface Service {

        /**
         * Gets aggregated content for the feed for the date provided.
         *
         * @param year four-digit year
         * @param month two-digit month
         * @param day two-digit day
         */
        @NonNull
        @Headers(ACCEPT_HEADER_PREFIX + "aggregated-feed/0.5.0\"")
        @GET("feed/featured/{year}/{month}/{day}")
        Call<AggregatedFeedContent> get(@Header("X-Lang") String lang,
                                        @Path("year") String year,
                                        @Path("month") String month,
                                        @Path("day") String day);
    }

    private abstract static class BaseClient implements FeedClient, retrofit2.Callback<AggregatedFeedContent> {
        @NonNull private AggregatedFeedContentClient aggregatedClient;
        @Nullable private Callback cb;
        private WikiSite wiki;
        private int age;

        BaseClient(@NonNull AggregatedFeedContentClient aggregatedClient) {
            this.aggregatedClient = aggregatedClient;
        }

        abstract void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                          @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards);

        @Override
        public void request(@NonNull Context context, @NonNull WikiSite wiki, int age, @NonNull Callback cb) {
            this.cb = cb;
            this.age = age;
            this.wiki = wiki;
            if (aggregatedClient.aggregatedResponseAge == age
                    && aggregatedClient.aggregatedResponses.containsKey(wiki.languageCode())) {
                List<Card> cards = new ArrayList<>();
                getCardFromResponse(aggregatedClient.aggregatedResponses, wiki, age, cards);
                FeedCoordinator.postCardsToCallback(cb, cards);
            } else {
                aggregatedClient.requestAggregated(age, this);
            }
        }

        @Override
        public void cancel() {
        }

        @Override public void onResponse(@NonNull Call<AggregatedFeedContent> call,
                                         @NonNull Response<AggregatedFeedContent> response) {
            aggregatedClient.numResponsesReceived++;
            AggregatedFeedContent content = response.body();
            if (content == null) {
                if (cb != null) {
                    cb.error(new RuntimeException("Aggregated response was not in the correct format."));
                }
                return;
            }
            aggregatedClient.addAggregatedResponse(content, age, WikiSite.forLanguageCode(call.request().header("X-Lang")));
            if (aggregatedClient.numResponsesReceived < aggregatedClient.numResponsesExpected) {
                return;
            }

            List<Card> cards = new ArrayList<>();
            if (aggregatedClient.aggregatedResponses.containsKey(wiki.languageCode())) {
                getCardFromResponse(aggregatedClient.aggregatedResponses, wiki, age, cards);
            }
            if (cb != null) {
                FeedCoordinator.postCardsToCallback(cb, cards);
            }
        }

        @Override public void onFailure(@NonNull Call<AggregatedFeedContent> call, @NonNull Throwable caught) {
            if (call.isCanceled()) {
                return;
            }
            L.v(caught);
            aggregatedClient.numResponsesReceived++;
            if (aggregatedClient.numResponsesReceived < aggregatedClient.numResponsesExpected) {
                return;
            }
            if (cb != null) {
                cb.error(caught);
            }
        }
    }
}
