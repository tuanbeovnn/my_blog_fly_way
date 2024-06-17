const {postData, getCategoryList, commentsData} = require('./axiosClient');
const postTemplate = require('./postTemplate.json'); // Import JSON file
const commentTemplate = require('./commentTemplate.json');
const moment = require('moment');

// Function to generate random category ID from an array
const getRandomCategoryId = (categoryIds) => {
    const randomIndex = Math.floor(Math.random() * categoryIds.length);
    return categoryIds[randomIndex];
};

// Function to calculate the date with offset
const calculateDateWithOffset = (offset) => {
    if (offset.startsWith('{') && offset.endsWith(' days}')) {
        const daysOffset = parseInt(offset.substring(1, offset.length - 6));
        if (!isNaN(daysOffset)) {
            return moment().subtract(daysOffset, 'days').toISOString();
        }
    }
    return null;
};

async function main() {
    const args = process.argv.slice(2);
    let action = 'test'; // Default action is set to "test"

    if (args.length >= 3 && args[0] === 'Post') {
        // If the action parameter is provided, use it
        if (args.length === 4) {
            action = args[3].toLowerCase();
        }

        const url = args[1];
        const token = args[2];

        try {
            // Fetch category IDs dynamically
            const categories = await getCategoryList(url, token);

            const categoryIds = categories.details.records.map(category => category.id);

            const {posts} = postTemplate; // Access posts array from JSON

            for (const post of posts) {
                // Modify the categoryId field with a random category ID
                post.categoryId = getRandomCategoryId(categoryIds);

                // Set createdDate based on the provided offset, if available
                if (post.createdDate) {
                    const calculatedDate = calculateDateWithOffset(post.createdDate);
                    if (calculatedDate) {
                        post.createdDate = calculatedDate;
                    } else {
                        console.log('Invalid createdDate offset format:', post.createdDate);
                    }
                }

                await postData(url, post, token);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    } else if (args.length >= 3 && args[0] === 'Comment') {
        if (args.length === 4) {
            action = args[3].toLowerCase();
        }
        const url = args[1];
        const token = args[2];
        try {
            const {comments} = commentTemplate;

            for (const comment of comments) {
                await commentsData(url, comment, token);
            }
        } catch (error) {
            console.error('Error:', error);
        }

    } else {
        console.log('Usage: node generateTestData Post <URL> <Token> [test|production]');
    }
}

main();
