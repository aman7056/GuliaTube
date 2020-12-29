package com.amatiandroid.basicbrowser

import android.app.Activity
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener


class MainActivity : AppCompatActivity(), RewardedVideoAdListener {
    lateinit var web_browser : WebView
    lateinit var  search_btn : ImageButton
    lateinit var url_txt : EditText
    lateinit var back_btn : ImageButton
    lateinit var fab_btn : FloatingActionButton
    private val STORAGE_PERMISSION_CODE: Int = 1000
    lateinit var mAdView : AdView
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private lateinit var mInterstitialAd: InterstitialAd



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this){}
        MobileAds.initialize(this, "ca-app-pub-3940256099942544/5224354917")
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.")
        }

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val adSize = AdSize(300, 50)


        web_browser = findViewById(R.id.webview)
        search_btn = findViewById(R.id.search_btn)
        back_btn = findViewById(R.id.return_btn)
        url_txt = findViewById(R.id.url)
        fab_btn = findViewById(R.id.fabUrl)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){

                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)

            }
            else
            {
                web_browser.loadUrl("http://www.youtube.com")
            }
        }

        web_browser.settings.javaScriptEnabled = true // we need to enable javascript
        web_browser.canGoBack()
        web_browser.webViewClient = WebClient(this)
        web_browser.settings.loadWithOverviewMode = true
        web_browser.settings.useWideViewPort = true
        web_browser.settings.domStorageEnabled = true
        web_browser.settings.databaseEnabled = true
        // Now we need to load an url everytime we search something
        search_btn.setOnClickListener {
            val URL = url_txt.text.toString()
            web_browser.loadUrl(URL)
        }

        fab_btn.setOnClickListener{
            loadRewardedVideoAd()
        }



        //now we will add the script to return back
        back_btn.setOnClickListener {
            web_browser.goBack()
        }


        web_browser.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG).show()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType))
        }

        //before we test our app we need to create a webclient class

        mInterstitialAd.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()}
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
            }
        }

    }


    class WebClient internal constructor(private val activity: Activity):WebViewClient(){
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            view?.loadUrl(request?.url.toString())
            return true
        }

    }

    override fun onBackPressed() {
        if (web_browser.canGoBack())
            web_browser.goBack()
        else
            super.onBackPressed()

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode){
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED)

                    web_browser.loadUrl("http://www.youtube.com")

            }else -> {
            Toast.makeText(this,"Permission Denied!", Toast.LENGTH_LONG).show()
        }
        }
    }

    override fun onRewarded(reward: RewardItem) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Link", web_browser.url.toString())
        clipboardManager.setPrimaryClip(clipData)
        // Reward the user.
        web_browser.loadUrl("https://yt1s.com/")
    }

    override fun onRewardedVideoAdLeftApplication() {
        Toast.makeText(this, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdClosed() {

    }

    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
        Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdLoaded() {
        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show()
        if (mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.show()
        }
    }

    override fun onRewardedVideoAdOpened() {

    }

    override fun onRewardedVideoStarted() {

    }

    override fun onRewardedVideoCompleted() {

    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                AdRequest.Builder().build())
    }
}
