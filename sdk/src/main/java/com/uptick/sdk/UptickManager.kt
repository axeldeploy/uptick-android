package com.uptick.sdk

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import coil.load
import com.uptick.sdk.model.OfferData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UptickManager {
    private val network by lazy {
        Network().apiService
    }
    private var integrationId = ""
    private var flowId = ""
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }
    private var scope = CoroutineScope(coroutineExceptionHandler)
    private var primaryColor = Color.parseColor("#5bb85d")
    private var secondaryColor = Color.parseColor("#efefef")
    private var bgColor = Color.parseColor("#4D000000")

    fun setPrimaryColor(@ColorInt color: Int) {
        primaryColor = color
    }

    fun setSecondaryColor(@ColorInt color: Int) {
        secondaryColor = color
    }

    fun setBgColor(@ColorInt color: Int) {
        bgColor = color
    }

    private var container: FrameLayout? = null
    private var context: Context? = null
    fun initiateView(context: Context, container: FrameLayout, integrationId: String) {
        this.context = context
        this.container = container
        this.integrationId = integrationId

        scope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val flow = network.newFlow(integrationId)
            if (flow.isSuccessful) {
                flow.body()?.data?.let {
                    it.find { it.type == "flow" }?.let {
                        flowId = it.id
                        showOffer()
                    }
                }
            }
        }
    }

    private fun showOffer() {
        scope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val response = network.nextOffer(integrationId, flowId)
            if (response.isSuccessful) response.body()?.let {
                if (it.data.isEmpty()) launch(Dispatchers.Main + coroutineExceptionHandler) { container?.removeAllViews() }
                it.data.find { it.type == "offer" }?.let { uptickItem ->
                    uptickItem.attributes?.let { offer ->
                        showOfferView(offer)
                    }
                }
            }
        }
    }

    private fun showOfferView(offer: OfferData) {
        scope.launch(Dispatchers.Main + coroutineExceptionHandler) {
            val parentContainer = FrameLayout(context!!).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(bgColor)
            }
            val cardView = CardView(context!!).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    setMargins(16.dpToPx(), 0, 16.dpToPx(), 0)
                }
                cardElevation = 8.dpToPx().toFloat()
                setCardBackgroundColor(Color.WHITE)
            }
            val linearLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            cardView.addView(linearLayout)
            parentContainer.addView(cardView)

            // Header
            offer.header?.forEach {
                if (it.type == "text") {
                    val headerTextView = android.widget.TextView(context).apply {
                        text = it.text
                        textSize = 24f
                        setTextColor(android.graphics.Color.WHITE)
                        setBackgroundColor(primaryColor)
                        setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
                    }
                    linearLayout.addView(headerTextView)
                }
            }
            //image
            offer.image?.let { image ->
                val imageView = android.widget.ImageView(context).apply {
                    load(image.url)
                    scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                    setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 8.dpToPx())
                    maxHeight = 56.dpToPx()
                }
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                linearLayout.addView(imageView, params)
            }

            // content
            var contentString: CharSequence? = null
            offer.content?.forEach {
                if (it.type == "text") {
                    val text = android.text.SpannableString(it.text).apply {
                        if (it.attributes?.emphasis == "bold") boldSpan(it.text)
                    }
                    contentString = android.text.TextUtils.concat(contentString ?: "", text)
                }
            }
            contentString?.let {
                val contentTextView = android.widget.TextView(context).apply {
                    text = contentString
                    textSize = 16f
                    setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
                }
                linearLayout.addView(contentTextView)
            }

            // Actions
            offer.actions?.forEach { item ->
                if (item.type == "button") {
                    val button = android.widget.Button(context).apply {
                        text = item.text
                        setBackgroundColor(if (item.attributes?.kind == "primary") primaryColor else secondaryColor)
                        setTextColor(
                            if (item.attributes?.kind == "primary") android.graphics.Color.WHITE else android.graphics.Color.parseColor(
                                "#191919"
                            )
                        )
                        setPadding(16.dpToPx(), 8, 16.dpToPx(), 8)
                        setOnClickListener { buttonView ->
                            item.attributes?.to?.let { link ->
                                if (link.contains("reject")) {
                                    buttonView.isEnabled = false
                                    showOffer()
                                } else {
                                    showOffer()
                                    context.openLink(link)
                                }
                            }
                        }
                    }
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                    linearLayout.addView(button, params)
                }
            }

            // Disclaimer
            offer.disclaimer?.forEach {
                if (it.type == "text") {
                    val disclaimerTextView = android.widget.TextView(context).apply {
                        text = it.text
                        textSize = 12f
                        setTextColor(android.graphics.Color.GRAY)
                        setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                    }
                    linearLayout.addView(disclaimerTextView)
                }
            }

            // Footer
            offer.footer?.forEach {
                if (it.type == "text") {
                    var footerString: CharSequence? = null
                    val footerTextView = android.widget.TextView(context).apply {
                        textSize = 10f
                        setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
                    }
                    it.children?.let { children ->
                        children.forEach {
                            val text = android.text.SpannableString(it.text).apply {
                                if (it.type == "link") clickableSpan(it.text) {
                                    context?.openLink(it.attributes?.to ?: "")
                                }
                            }
                            footerString = android.text.TextUtils.concat(footerString ?: "", text)
                        }
                        footerTextView.text = footerString
                        footerTextView.movementMethod =
                            android.text.method.LinkMovementMethod.getInstance()
                        footerTextView.gravity = Gravity.END
                        linearLayout.addView(footerTextView)
                    }

                }
            }
            container?.let {
                container?.removeAllViews()
                it.addView(parentContainer)
            }
        }
    }
}