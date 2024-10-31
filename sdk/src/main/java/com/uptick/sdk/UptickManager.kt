package com.uptick.sdk

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import coil.load
import com.uptick.sdk.model.Placement
import com.uptick.sdk.model.UptickResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

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
    private val lightGrey = Color.parseColor("#909090")
    private var placement = Placement.ORDER_CONFIRMATION
    private var optionalParams = mapOf<String,String>()
    var onError: (String) -> Unit = {}

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
    fun initiateView(
        context: Context,
        container: FrameLayout,
        integrationId: String,
        placement: Placement = Placement.ORDER_CONFIRMATION,
        optionalParams:Map<String,String> = mapOf()
    ) {
        this.context = context
        this.container = container
        this.integrationId = integrationId
        this.placement = placement
        this.optionalParams = optionalParams

        scope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val flow = network.newFlow(
                integrationId, this@UptickManager.placement.value,
                optionalParams
            )
            if (flow.isSuccessful) {
                flow.body()?.data?.let {
                    it.find { it.type == "flow" }?.let {
                        flowId = it.id
                        showOffer()
                    }
                }
            } else {
                flow.errorBody()?.let {
                    try {
                        handleError(JSONObject(it.string()).getString("error"))
                    } catch (e: Exception) {
                        e.localizedMessage?.let { message ->
                            handleError(message)
                        }
                    }
                }
            }
        }
    }

    private fun showOffer() {
        scope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val response = network.nextOffer(integrationId, flowId, placement.value, options = optionalParams)
            if (response.isSuccessful) response.body()?.let {
                showOfferView(it)
            } else {
                response.errorBody()?.let {
                    try {
                        handleError(
                            JSONObject(it.string()).getJSONArray("errors").getJSONObject(0)
                                .getString("title")
                        )
                    } catch (e: Exception) {
                        e.localizedMessage?.let { message ->
                            handleError(message)
                        }
                    }
                }
            }
        }
    }

    private fun handleError(error: String) {
        scope.launch(Dispatchers.Main + coroutineExceptionHandler) {
            onError(error)
        }
    }

    private fun showOfferView(response: UptickResponse) {
        scope.launch(Dispatchers.Main + coroutineExceptionHandler) {
            response.data.find { it.type == "offer" }?.attributes?.let { offer ->
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
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                cardView.addView(linearLayout)
                parentContainer.addView(cardView)

                // Header
                offer.header?.forEach {
                    if (it.type == "text") {
                        val headerTextView = TextView(context).apply {
                            includeFontPadding = false
                            text = it.text
                            setTextSize(it.attributes?.size)
                            setTextColor(getTextColor(it.attributes?.appearance) ?: Color.WHITE)
                            setTextStyle(it.attributes?.emphasis)
                            setBackgroundColor(primaryColor)
                            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
                        }
                        linearLayout.addView(headerTextView)
                    }
                }
                // Digits
                offer.offers?.let { offerDigits ->
                    val digitsContainer = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.gravity = Gravity.CENTER
                        params.setMargins(0, 16.dpToPx(), 0, 16.dpToPx())
                        layoutParams = params
                        dividerDrawable = GradientDrawable().apply {
                            setSize(8.dpToPx(), 8.dpToPx())
                        }
                        showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                    }
                    for (i in offerDigits.start..offerDigits.size) {
                        val digit = TextView(context).apply {
                            width = 32.dpToPx()
                            height = 32.dpToPx()
                            text = i.toString()
                            textSize = 12f
                            setTextColor(Color.WHITE)
                            background =
                                createCircle(if (offerDigits.current >= i) primaryColor else secondaryColor)
                            gravity = Gravity.CENTER
                        }
                        digitsContainer.addView(digit)
                    }
                    linearLayout.addView(digitsContainer)
                }
                //image
                offer.image?.let { image ->
                    val imageView = android.widget.ImageView(context).apply {
                        load(image.url)
                        scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                        setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                    }
                    val params = LinearLayout.LayoutParams(
                        100.dpToPx(),
                        100.dpToPx()
                    )
                    params.gravity = Gravity.CENTER
                    linearLayout.addView(imageView, params)
                }
                //personalization
                offer.personalization?.forEach {
                    if (it.type == "text") {
                        val disclaimerTextView = TextView(context).apply {
                            gravity = Gravity.START
                            includeFontPadding = false

                            text = it.text
                            setTextSize(it.attributes?.size)
                            setTextStyle(it.attributes?.emphasis)
                            setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                        }
                        linearLayout.addView(disclaimerTextView)
                    }
                }
                //sponsored
                offer.sponsored?.forEach {
                    if (it.type == "text") {
                        val sponsoredTextView = TextView(context).apply {
                            includeFontPadding = false
                            text = it.text
                            setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                            setTextSize(it.attributes?.size)
                            setTextColor(getTextColor(it.attributes?.appearance) ?: lightGrey)
                            setTextStyle(it.attributes?.emphasis)
                        }
                        linearLayout.addView(sponsoredTextView)
                    }
                }

                // content
                var contentString: CharSequence? = null
                offer.content?.forEach {
                    if (it.type == "text") {
                        val mText = it.text
                        val text = android.text.SpannableString(mText).apply {
                            if (it.attributes?.emphasis == "bold") boldSpan(mText)
                        }
                        contentString = android.text.TextUtils.concat(contentString ?: "", text)
                    }
                }
                contentString?.let {
                    val contentTextView = TextView(context).apply {
                        includeFontPadding = false
                        minHeight = 0
                        text = contentString
                        textSize = 16f
                        setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
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
                                if (item.attributes?.kind == "primary") Color.WHITE else Color.parseColor(
                                    "#191919"
                                )
                            )
                            setOnClickListener { buttonView ->
                                item.attributes?.to?.let { link ->
                                    if (link.contains("accept")) {
                                        showOffer()
                                        context.openLink(link)
                                    }
                                }
                                item.attributes?.url?.let { link ->
                                    if (link.contains("reject")) {
                                        buttonView.isEnabled = false
                                        showOffer()
                                    }
                                }
                            }
                        }
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(16.dpToPx(), 0, 16.dpToPx(), 8.dpToPx())
                        linearLayout.addView(button, params)
                    }
                }

                // Disclaimer
                offer.disclaimer?.forEach {
                    if (it.type == "text") {
                        val disclaimerTextView = TextView(context).apply {
                            gravity = Gravity.START
                            includeFontPadding = false

                            text = it.text
                            setTextSize(it.attributes?.size)
                            setTextColor(getTextColor(it.attributes?.appearance) ?: Color.GRAY)
                            setTextStyle(it.attributes?.emphasis)
                            setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                        }
                        linearLayout.addView(disclaimerTextView)
                    }
                }

                // Footer
                offer.footer?.forEach {
                    if (it.type == "text") {
                        var footerString: CharSequence? = null
                        val footerTextView = TextView(context).apply {
                            includeFontPadding = false
                            setTextSize(it.attributes?.size)
                            setTextColor(getTextColor(it.attributes?.appearance) ?: Color.GRAY)
                            setTextStyle(it.attributes?.emphasis)
                            setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 16.dpToPx())
                        }
                        it.children?.let { children ->
                            children.forEach {
                                val text = android.text.SpannableString(it.text).apply {
                                    if (it.type == "link") clickableSpan(it.text) {
                                        context?.openLink(it.attributes?.to ?: "")
                                    }
                                }
                                footerString =
                                    android.text.TextUtils.concat(footerString ?: "", text)
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
            } ?: run {
                container?.removeAllViews()
            }
        }
    }

    private fun createCircle(color: Int): GradientDrawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.OVAL
        shape.cornerRadii = floatArrayOf(100f, 100f, 100f, 100f, 100f, 100f, 100f, 100f)
        shape.setColor(color)
        return shape
    }

    private fun TextView.setTextStyle(emphasis: String?) {
        emphasis?.let {
            when (it) {
                "bold" -> setTypeface(typeface, Typeface.BOLD)
                "italic" -> setTypeface(typeface, Typeface.ITALIC)
            }
        }
    }

    private fun TextView.setTextSize(size: String?) {
        textSize = when (size) {
            "extraSmall" -> 10f
            "small" -> 12f
            "large" -> 24f
            else -> 16f
        }
    }

    private fun getTextColor(color: String?): Int? {
        return when (color) {
            "accent" -> Color.WHITE
            "subdued" -> Color.parseColor("#585858")
            else -> null
        }
    }

    /* private fun offerViewed(url: String) {
         scope.launch(Dispatchers.IO + coroutineExceptionHandler) {
             network.offerEvent(url)
         }
     }*/
}