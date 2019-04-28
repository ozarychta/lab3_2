package edu.iis.mto.staticmock;

import edu.iis.mto.staticmock.reader.NewsReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationLoader.class, NewsReaderFactory.class})
public class NewsLoaderTest {

    NewsLoader newsLoader;
    ConfigurationLoader configurationLoader;
    NewsReader newsReader;
    IncomingNews incomingNews;

    @Before
    public void setUp(){
        PowerMockito.mockStatic(ConfigurationLoader.class, NewsReaderFactory.class);

        Configuration configuration = new Configuration();
        configurationLoader = mock(ConfigurationLoader.class);
        when(ConfigurationLoader.getInstance()).thenReturn(configurationLoader);
        when(configurationLoader.loadConfiguration()).thenReturn(configuration);

        newsReader = mock(NewsReader.class);
        when(NewsReaderFactory.getReader(anyString())).thenReturn(newsReader);

        incomingNews = new IncomingNews();

        when(newsReader.read()).thenReturn(incomingNews);

        newsLoader = new NewsLoader();
    }

    @Test
    public void loadNews_emptyNewsShouldBeCorrectlyDividedIntoPublicOrSubscriberContent() {
        PublishableNews publishableNews = newsLoader.loadNews();

        List<String> publicInfo = Whitebox.getInternalState(publishableNews, "publicContent");
        List<String> forSubscription = Whitebox.getInternalState(publishableNews, "subscribentContent");

        assertThat(publicInfo.size(), is(0));
        assertThat(forSubscription.size(), is(0));
    }

    @Test
    public void loadNews_notEmptyNewsShouldBeCorrectlyDividedIntoPublicOrSubscriberContent() {
        IncomingInfo incomingInfo1 = new IncomingInfo("content 1", SubsciptionType.A);
        IncomingInfo incomingInfo2 = new IncomingInfo("content 2", SubsciptionType.B);
        IncomingInfo incomingInfo3 = new IncomingInfo("content 3", SubsciptionType.C);
        IncomingInfo incomingInfo4 = new IncomingInfo("content 44", SubsciptionType.NONE);
        incomingNews.add(incomingInfo1);
        incomingNews.add(incomingInfo2);
        incomingNews.add(incomingInfo3);
        incomingNews.add(incomingInfo4);

        PublishableNews publishableNews = newsLoader.loadNews();

        List<String> publicInfo = Whitebox.getInternalState(publishableNews, "publicContent");
        List<String> forSubscription = Whitebox.getInternalState(publishableNews, "subscribentContent");

        assertThat(publicInfo.size(), is(1));
        assertThat(forSubscription.size(), is(3));
    }

    @Test
    public void loadNews_loadConfigurationShouldBeCalledOnce() {
        newsLoader.loadNews();

        verify(configurationLoader).loadConfiguration();
    }

    @Test
    public void loadNews_readShouldBeCalledOnce() {
        newsLoader.loadNews();

        verify(newsReader).read();
    }
}